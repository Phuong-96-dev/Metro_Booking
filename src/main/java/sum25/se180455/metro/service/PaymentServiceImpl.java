package sum25.se180455.metro.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sum25.se180455.metro.pojo.Member;
import sum25.se180455.metro.pojo.Payment;
import sum25.se180455.metro.pojo.Ticket;
import sum25.se180455.metro.repository.PaymentRepository;
import sum25.se180455.metro.request.PaymentRequest;

import java.time.LocalDate;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Override
    public Payment processPayment(PaymentRequest request) {
        Ticket ticket = ticketService.findById(request.getTicketId());
        Member member = memberService.findById(request.getMemberId());

        if (ticket == null) {
            throw new RuntimeException("Không tìm thấy vé");
        }

        if (member == null) {
            throw new RuntimeException("Không tìm thấy thành viên");
        }

        if ("Đã thanh toán".equalsIgnoreCase(ticket.getStatus())) {
            throw new RuntimeException("Vé đã được thanh toán");
        }

        Payment payment = new Payment();
        payment.setPaymentDate(LocalDate.now());
        payment.setPrice(ticket.getPrice());
        payment.setStatus("PAID");
        payment.setTicket(ticket);
        payment.setMember(member);

        paymentRepository.save(payment);

        return payment;
    }

    @Override
    public Payment save(Payment payment) {
        return paymentRepository.save(payment);
    }
}
