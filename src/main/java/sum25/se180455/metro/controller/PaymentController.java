package sum25.se180455.metro.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sum25.se180455.metro.pojo.Member;
import sum25.se180455.metro.pojo.Payment;
import sum25.se180455.metro.pojo.Ticket;
import sum25.se180455.metro.request.SePayCheckout;
import sum25.se180455.metro.service.PaymentService;
import sum25.se180455.metro.service.TicketService;

import java.util.Map;

@Controller
public class PaymentController {
    private final PaymentService paymentService;
    private final TicketService ticketService;

    public PaymentController(PaymentService paymentService, TicketService ticketService) {
        this.paymentService = paymentService;
        this.ticketService = ticketService;
    }

    @GetMapping("/payment")
    public String showPaymentPage(@RequestParam Long ticketId,
                                  HttpSession session,
                                  Model model,
                                  RedirectAttributes redirect) {
        Member member = (Member) session.getAttribute("member");
        if (member == null) {
            return "redirect:/login";
        }
        Ticket ticket = ticketService.findById(ticketId);
        if (!isOwned(ticket, member)) {
            redirect.addFlashAttribute("error", "Vé không tồn tại hoặc không thuộc tài khoản của bạn.");
            return "redirect:/ticket";
        }

        Payment existing = paymentService.findOwnedPayment(ticketId, member.getMemberId()).orElse(null);
        if (existing != null && "PAID".equalsIgnoreCase(existing.getStatus())) {
            return "redirect:/payment/success?ticketId=" + ticketId;
        }
        if (!"Chưa thanh toán".equalsIgnoreCase(ticket.getStatus())) {
            redirect.addFlashAttribute("error", "Vé không còn ở trạng thái chờ thanh toán.");
            return "redirect:/myTicket";
        }

        model.addAttribute("member", member);
        model.addAttribute("ticket", ticket);
        model.addAttribute("sepayConfigured", paymentService.isSePayConfigured());
        if (!paymentService.isSePayConfigured()) {
            model.addAttribute("error", "SePay chưa được cấu hình trên máy chủ. Vui lòng liên hệ quản trị viên.");
            return "member/payment";
        }
        try {
            SePayCheckout checkout = paymentService.prepareSePayCheckout(ticketId, member.getMemberId());
            model.addAttribute("checkout", checkout);
        } catch (IllegalArgumentException | IllegalStateException | SecurityException exception) {
            model.addAttribute("error", exception.getMessage());
        }
        return "member/payment";
    }

    @PostMapping("/payment")
    public String processPayment(@RequestParam Long ticketId,
                                 HttpSession session,
                                 RedirectAttributes redirect) {
        Member member = (Member) session.getAttribute("member");
        if (member == null) {
            return "redirect:/login";
        }
        try {
            paymentService.prepareSePayCheckout(ticketId, member.getMemberId());
            return "redirect:/payment?ticketId=" + ticketId;
        } catch (IllegalArgumentException | IllegalStateException | SecurityException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
            return "redirect:/ticket";
        }
    }

    @GetMapping("/api/payments/{ticketId}/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> paymentStatus(@PathVariable Long ticketId, HttpSession session) {
        Member member = (Member) session.getAttribute("member");
        if (member == null) {
            return ResponseEntity.status(401).body(Map.of("status", "UNAUTHENTICATED"));
        }
        return paymentService.findOwnedPayment(ticketId, member.getMemberId())
                .<ResponseEntity<Map<String, Object>>>map(payment -> ResponseEntity.ok(Map.of(
                        "status", payment.getStatus(),
                        "paid", "PAID".equalsIgnoreCase(payment.getStatus()),
                        "redirectUrl", "/payment/success?ticketId=" + ticketId)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/payment/success")
    public String showSuccessPage(@RequestParam Long ticketId, Model model, HttpSession session) {
        Member member = (Member) session.getAttribute("member");
        if (member == null) {
            return "redirect:/login";
        }
        Ticket ticket = ticketService.findById(ticketId);
        Payment payment = paymentService.findOwnedPayment(ticketId, member.getMemberId()).orElse(null);
        if (!isOwned(ticket, member) || payment == null || !"PAID".equalsIgnoreCase(payment.getStatus())) {
            return "redirect:/payment?ticketId=" + ticketId;
        }
        model.addAttribute("ticket", ticket);
        model.addAttribute("payment", payment);
        return "member/success";
    }

    private boolean isOwned(Ticket ticket, Member member) {
        return ticket != null && ticket.getMember() != null
                && ticket.getMember().getMemberId().equals(member.getMemberId());
    }
}
