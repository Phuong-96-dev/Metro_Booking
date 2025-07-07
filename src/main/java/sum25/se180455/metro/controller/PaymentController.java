package sum25.se180455.metro.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sum25.se180455.metro.pojo.Member;
import sum25.se180455.metro.pojo.Payment;
import sum25.se180455.metro.pojo.Ticket;
import sum25.se180455.metro.request.PaymentRequest;
import sum25.se180455.metro.service.MemberService;
import sum25.se180455.metro.service.PaymentService;
import sum25.se180455.metro.service.TicketService;

import java.time.LocalDate;
import java.util.List;
@Controller
public class PaymentController {
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private TicketService ticketService;
    @Autowired
    MemberService memberService;

    @GetMapping("/tickets/unpaid/{memberId}")
    public ResponseEntity<List<Ticket>> getUnpaidTickets(@PathVariable Long memberId) {
        List<Ticket> unpaidTickets = ticketService.getUnpaidTickets(memberId);
        return ResponseEntity.ok(unpaidTickets);
    }

    @GetMapping("/payment")
    public String showPaymentPage(HttpSession session, Model model) {
        Member member = (Member) session.getAttribute("member");

        if (member == null) {
            return "redirect:/login";
        }

        List<Ticket> unpaidTickets = ticketService.getUnpaidTickets(member.getMemberId());
        model.addAttribute("unpaidTickets", unpaidTickets);
        model.addAttribute("member", member);

        return "member/payment";

    }

    @PostMapping("/payment")
    public String processPayment(
            @RequestParam("ticketId") Long ticketId,
            @RequestParam("memberId") Long memberId,
            RedirectAttributes redirectAttributes
    ) {
        Ticket ticket = ticketService.findById(ticketId);
        Member member = memberService.findById(memberId);

        if (ticket == null || member == null) {
            redirectAttributes.addFlashAttribute("error", "Vé hoặc thành viên không tồn tại.");
            return "redirect:/payment";
        }

        if ("Active".equalsIgnoreCase(ticket.getStatus())) {
            redirectAttributes.addFlashAttribute("error", "Vé đã được thanh toán.");
            return "redirect:/payment";
        }

        Payment payment = new Payment();
        payment.setPaymentDate(LocalDate.now());
        payment.setPrice(ticket.getPrice());
        payment.setStatus("Active");
        payment.setTicket(ticket);
        payment.setMember(member);
        paymentService.save(payment);

        ticket.setStatus("Active");
        ticketService.save(ticket);

        redirectAttributes.addFlashAttribute("success", "Thanh toán thành công!");
        return "redirect:/payment";
    }

}
