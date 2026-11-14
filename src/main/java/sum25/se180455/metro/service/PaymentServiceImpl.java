package sum25.se180455.metro.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;
import sum25.se180455.metro.config.SePayProperties;
import sum25.se180455.metro.pojo.Member;
import sum25.se180455.metro.pojo.Payment;
import sum25.se180455.metro.pojo.Ticket;
import sum25.se180455.metro.repository.PaymentRepository;
import sum25.se180455.metro.repository.TicketRepository;
import sum25.se180455.metro.request.PaymentRequest;
import sum25.se180455.metro.request.SePayCheckout;
import sum25.se180455.metro.request.SePayWebhookPayload;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PaymentServiceImpl implements PaymentService {
    private static final String PENDING = "PENDING";
    private static final String PAID = "PAID";
    private static final char[] CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();

    private final SecureRandom random = new SecureRandom();
    private final TicketService ticketService;
    private final MemberService memberService;
    private final PaymentRepository paymentRepository;
    private final TicketRepository ticketRepository;
    private final ScanTicketService scanTicketService;
    private final SePayProperties sePay;

    public PaymentServiceImpl(TicketService ticketService,
                              MemberService memberService,
                              PaymentRepository paymentRepository,
                              TicketRepository ticketRepository,
                              ScanTicketService scanTicketService,
                              SePayProperties sePay) {
        this.ticketService = ticketService;
        this.memberService = memberService;
        this.paymentRepository = paymentRepository;
        this.ticketRepository = ticketRepository;
        this.scanTicketService = scanTicketService;
        this.sePay = sePay;
    }

    @Override
    @Transactional
    public Payment processPayment(PaymentRequest request) {
        Member member = memberService.findById(request.getMemberId());
        if (member == null) {
            throw new IllegalArgumentException("Không tìm thấy thành viên.");
        }
        return prepareSePayCheckout(request.getTicketId(), member.getMemberId()).payment();
    }

    @Override
    public Payment save(Payment payment) {
        return paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public SePayCheckout prepareSePayCheckout(Long ticketId, Long memberId) {
        requireConfigured();
        Ticket ticket = ticketRepository.findByIdForUpdate(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vé."));
        if (ticket.getMember() == null || !ticket.getMember().getMemberId().equals(memberId)) {
            throw new SecurityException("Bạn không có quyền thanh toán vé này.");
        }

        Payment payment = paymentRepository.findByTicket_TicketId(ticketId).orElse(null);
        if (payment != null && PAID.equalsIgnoreCase(payment.getStatus())) {
            return checkout(payment);
        }
        if (!"Chưa thanh toán".equalsIgnoreCase(ticket.getStatus())) {
            throw new IllegalStateException("Vé không còn ở trạng thái chờ thanh toán.");
        }
        if (payment == null) {
            payment = new Payment();
            payment.setTicket(ticket);
            payment.setMember(ticket.getMember());
            payment.setPrice(ticket.getPrice());
            payment.setPaymentMethod("SEPAY");
        }
        if (payment.getTransactionCode() == null || payment.getTransactionCode().isBlank()) {
            payment.setTransactionCode(generateTransactionCode(ticketId));
        }
        payment.setStatus(PENDING);
        payment = paymentRepository.saveAndFlush(payment);
        return checkout(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Payment> findOwnedPayment(Long ticketId, Long memberId) {
        return paymentRepository.findByTicket_TicketId(ticketId)
                .filter(payment -> payment.getMember() != null
                        && payment.getMember().getMemberId().equals(memberId));
    }

    @Override
    @Transactional
    public boolean confirmSePayPayment(SePayWebhookPayload payload) {
        if (payload == null || payload.id() == null || payload.transferAmount() == null
                || !"in".equalsIgnoreCase(payload.transferType())) {
            return false;
        }
        if (!normalizeAccount(sePay.getAccountNumber()).equals(normalizeAccount(payload.accountNumber()))) {
            return false;
        }
        String transactionCode = extractTransactionCode(payload);
        if (transactionCode == null) {
            return false;
        }
        Payment payment = paymentRepository.findByTransactionCodeForUpdate(transactionCode).orElse(null);
        if (payment == null) {
            return false;
        }
        if (payment.getSepayTransactionId() != null) {
            return payment.getSepayTransactionId().equals(payload.id());
        }
        if (paymentRepository.findBySepayTransactionId(payload.id()).isPresent()) {
            return true;
        }
        long expectedAmount = amount(payment.getPrice());
        if (payload.transferAmount() != expectedAmount) {
            return false;
        }

        payment.setSepayTransactionId(payload.id());
        payment.setPaymentContent(limit(payload.content(), 1000));
        payment.setBankReference(limit(payload.referenceCode(), 100));
        payment.setSepayGateway(limit(payload.gateway(), 50));
        payment.setTransferAmount(payload.transferAmount());
        payment.setPaidAt(LocalDateTime.now());
        payment.setPaymentDate(LocalDate.now());
        payment.setStatus(PAID);

        Ticket ticket = payment.getTicket();
        ticket.setStatus("Chưa sử dụng");
        ticketService.save(ticket);
        paymentRepository.saveAndFlush(payment);
        scanTicketService.issueForMetroTicket(ticket);
        return true;
    }

    @Override
    public boolean isSePayConfigured() {
        return sePay.isConfigured();
    }

    private SePayCheckout checkout(Payment payment) {
        long amount = amount(payment.getPrice());
        String description = sePay.transferDescription(payment.getTransactionCode());
        String qrUrl = UriComponentsBuilder.fromHttpUrl("https://vietqr.app/img")
                .queryParam("acc", sePay.getAccountNumber())
                .queryParam("bank", sePay.getBank())
                .queryParam("amount", amount)
                .queryParam("des", description)
                .queryParam("template", "compact")
                .queryParam("showinfo", "true")
                .queryParam("holder", sePay.getAccountHolder())
                .queryParam("store", "METRO")
                .build().encode().toUriString();
        return new SePayCheckout(payment, qrUrl, sePay.getBank(), sePay.getAccountNumber(),
                sePay.getAccountHolder(), description, amount);
    }

    private String generateTransactionCode(Long ticketId) {
        String prefix = sePay.normalizedPaymentPrefix();
        for (int attempt = 0; attempt < 10; attempt++) {
            StringBuilder code = new StringBuilder(prefix).append(ticketId);
            for (int i = 0; i < 6; i++) {
                code.append(CODE_ALPHABET[random.nextInt(CODE_ALPHABET.length)]);
            }
            String value = code.toString();
            if (!paymentRepository.existsByTransactionCode(value)) {
                return value;
            }
        }
        throw new IllegalStateException("Không tạo được mã thanh toán duy nhất.");
    }

    private String extractTransactionCode(SePayWebhookPayload payload) {
        String direct = normalize(payload.code());
        if (direct != null && direct.startsWith(sePay.normalizedPaymentPrefix())) {
            return direct;
        }
        String content = normalize(payload.content());
        if (content == null) {
            return null;
        }
        Pattern pattern = Pattern.compile("(?<![A-Z0-9])(" + Pattern.quote(sePay.normalizedPaymentPrefix())
                + "[A-Z0-9]{7,48})(?![A-Z0-9])");
        Matcher matcher = pattern.matcher(content);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.strip().toUpperCase(Locale.ROOT);
    }

    private String normalizeAccount(String value) {
        return value == null ? "" : value.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
    }

    private long amount(Double value) {
        if (value == null || value <= 0) {
            throw new IllegalStateException("Số tiền thanh toán không hợp lệ.");
        }
        return BigDecimal.valueOf(value).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    private String limit(String value, int maxLength) {
        if (value == null) return null;
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private void requireConfigured() {
        if (!sePay.isConfigured()) {
            throw new IllegalStateException("SePay chưa được cấu hình đầy đủ trên máy chủ.");
        }
    }
}
