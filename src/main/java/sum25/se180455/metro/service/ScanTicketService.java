package sum25.se180455.metro.service;

import sum25.se180455.metro.request.IssueTicketRequest;
import sum25.se180455.metro.request.ScanResponse;
import sum25.se180455.metro.pojo.*;
import sum25.se180455.metro.repository.ScanLogRepository;
import sum25.se180455.metro.repository.ScanTicketRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

@Service
public class ScanTicketService {

    private static final String METRO_ORDER_PREFIX = "METRO-TICKET-";
    private static final char[] ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
    private static final int CODE_RANDOM_LENGTH = 16;
    private static final Pattern CODE_PATTERN = Pattern.compile("^TK[A-Z2-9]{16}$");

    private final SecureRandom random = new SecureRandom();
    private final ScanTicketRepository scanTicketRepository;
    private final ScanLogRepository scanLogRepository;
    private final long defaultValidDays;

    public ScanTicketService(ScanTicketRepository scanTicketRepository,
                         ScanLogRepository scanLogRepository,
                         @Value("${app.ticket.default-valid-days:30}") long defaultValidDays) {
        this.scanTicketRepository = scanTicketRepository;
        this.scanLogRepository = scanLogRepository;
        this.defaultValidDays = defaultValidDays;
    }

    //1. CẤP VÉ sau khi VNPay xác nhận thanh toán thành công
    @Transactional
    public ScanTicket issueTicket(IssueTicketRequest req) {
        return scanTicketRepository.findByOrderRef(req.orderRef()).orElseGet(() -> create(req));
    }

    @Transactional
    public ScanTicket issueForMetroTicket(Ticket ticket) {
        if (ticket == null || ticket.getTicketId() == null || ticket.getMember() == null) {
            throw new IllegalArgumentException("Vé Metro chưa có đủ thông tin để tạo mã quét");
        }
        String eventName = ticket.getTicketType() == null || ticket.getTicketType().getName() == null
                ? "Vé Metro #" + ticket.getTicketId()
                : ticket.getTicketType().getName();
        return issueTicket(new IssueTicketRequest(
                ticket.getMember().getMemberId(),
                metroOrderRef(ticket.getTicketId()),
                eventName,
                ticket.getDate_end()));
    }

    public static String metroOrderRef(Long ticketId) {
        return METRO_ORDER_PREFIX + ticketId;
    }

    public static Long metroTicketId(String orderRef) {
        if (orderRef == null || !orderRef.startsWith(METRO_ORDER_PREFIX)) {
            return null;
        }
        try {
            return Long.valueOf(orderRef.substring(METRO_ORDER_PREFIX.length()));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private ScanTicket create(IssueTicketRequest req) {
        ScanTicket t = new ScanTicket();
        t.setOrderRef(req.orderRef());
        t.setUserId(req.userId());
        t.setEventName(req.eventName());
        t.setExpiresAt(req.expiresAt() != null ? req.expiresAt()
                : LocalDateTime.now().plusDays(defaultValidDays));
        t.setCode(generateUniqueCode());
        try {
            return scanTicketRepository.saveAndFlush(t);
        } catch (DataIntegrityViolationException e) {
            return scanTicketRepository.findByOrderRef(req.orderRef()).orElseThrow(() -> e);
        }
    }

    private String generateUniqueCode() {
        for (int i = 0; i < 5; i++) {
            StringBuilder sb = new StringBuilder("TK");
            for (int j = 0; j < CODE_RANDOM_LENGTH; j++) {
                sb.append(ALPHABET[random.nextInt(ALPHABET.length)]);
            }
            String code = sb.toString();
            if (!scanTicketRepository.existsByCode(code)) {
                return code;
            }
        }
        throw new IllegalStateException("Không sinh được mã vé duy nhất");
    }

    //2. QUÉT VÉ
    @Transactional
    public ScanResponse scan(String rawCode, String scannedBy) {
        String code = rawCode == null ? "" : rawCode.trim().toUpperCase();
        String scanner = (scannedBy == null || scannedBy.isBlank()) ? "unknown" : scannedBy.trim();
        if (scanner.length() > 100) {
            scanner = scanner.substring(0, 100);
        }

        if (!CODE_PATTERN.matcher(code).matches()) {
            String safeCode = code.length() > 100 ? code.substring(0, 100) : code;
            return finish(ScanResult.INVALID_FORMAT, "Mã không đúng định dạng", safeCode, null, scanner);
        }

        LocalDateTime now = LocalDateTime.now();


        int updated = scanTicketRepository.markUsed(code, now, scanner);
        ScanTicket ticket = scanTicketRepository.findByCode(code).orElse(null);

        if (updated == 1) {
            return finish(ScanResult.SUCCESS, "Quét thành công - vé hợp lệ", code, ticket, scanner);
        }
        if (ticket == null) {
            return finish(ScanResult.NOT_FOUND, "Vé không tồn tại", code, null, scanner);
        }
        return switch (ticket.getStatus()) {
            case USED -> finish(ScanResult.ALREADY_USED,
                    "Vé đã được sử dụng lúc " + ticket.getUsedAt(), code, ticket, scanner);
            case CANCELLED -> finish(ScanResult.CANCELLED, "Vé đã bị huỷ", code, ticket, scanner);
            case ACTIVE -> finish(ScanResult.EXPIRED,
                    "Vé đã hết hạn lúc " + ticket.getExpiresAt(), code, ticket, scanner);
        };
    }

    private ScanResponse finish(ScanResult result, String message, String code,
                                ScanTicket ticket, String scanner) {
        scanLogRepository.save(new ScanLog(code, result, scanner));
        return new ScanResponse(
                result == ScanResult.SUCCESS, result, message, code,
                ticket != null ? ticket.getEventName() : null,
                ticket != null ? ticket.getUserId() : null,
                ticket != null ? ticket.getUsedAt() : null);
    }

    //3. TRUY VẤN
    @Transactional(readOnly = true)
    public ScanTicket getByCode(String code) {
        return scanTicketRepository.findByCode(code)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy vé " + code));
    }

    @Transactional(readOnly = true)
    public List<ScanTicket> getByUser(Long userId) {
        return scanTicketRepository.findByUserIdOrderByIssuedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<ScanLog> recentLogs() {
        return scanLogRepository.findTop50ByOrderByScannedAtDesc();
    }

    @Transactional
    public ScanTicket cancel(String code) {
        ScanTicket t = getByCode(code);
        if (t.getStatus() != TicketStatus.ACTIVE) {
            throw new IllegalStateException("Chỉ huỷ được vé đang ACTIVE");
        }
        t.setStatus(TicketStatus.CANCELLED);
        return t;
    }
}
