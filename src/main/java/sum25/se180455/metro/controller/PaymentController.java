package sum25.se180455.metro.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
import java.util.Optional;

@Controller
public class PaymentController {
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private TicketService ticketService;
    @Autowired
    MemberService memberService;

    @GetMapping("/payment")
    public String showPaymentPage(@RequestParam(required = false) Long ticketId,
                                  HttpSession session,
                                  Model model) {
        Member member = (Member) session.getAttribute("member");

        if (member == null) {
            return "redirect:/login";
        }

        model.addAttribute("member", member);

        if (ticketId != null) {
            Ticket ticket = ticketService.findById(ticketId);

            // Kiểm tra vé thuộc về user và trạng thái chưa Active
            if (ticket != null
                    && ticket.getMember().getMemberId().equals(member.getMemberId())
                    && !"Active".equalsIgnoreCase(ticket.getStatus())) {

                model.addAttribute("ticket", ticket);
            } else {
                model.addAttribute("error", "Vé không hợp lệ hoặc đã thanh toán.");
                return "redirect:/ticket"; // chuyển về danh sách vé hoặc trang chính
            }
        } else {
            model.addAttribute("error", "Không tìm thấy vé cần thanh toán.");
            return "redirect:/ticket";
        }

        return "member/payment";
    }


    @PostMapping("/payment")
    public String processPayment(@RequestParam("ticketId") Long ticketId,
                                 @RequestParam("paymentMethod") String paymentMethod,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        Member member = (Member) session.getAttribute("member");

        if (member == null) {
            return "redirect:/login";
        }

        Ticket ticket = ticketService.findById(ticketId);
        if (ticket == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy vé.");
            return "redirect:/ticket";
        }

        if (!ticket.getMember().getMemberId().equals(member.getMemberId())
                || "ACTIVE".equalsIgnoreCase(ticket.getStatus())) {
            redirectAttributes.addFlashAttribute("error", "Vé không hợp lệ hoặc đã thanh toán.");
            return "redirect:/ticket";
        }

        // Cập nhật trạng thái vé
        ticket.setStatus("ACTIVE");
        ticketService.save(ticket);

        // Tạo payment và lưu
        Payment payment = new Payment();
        payment.setTicket(ticket);
        payment.setMember(member);
        payment.setPrice(ticket.getPrice());
        payment.setPaymentMethod(paymentMethod);
        payment.setStatus("SUCCESS");
        payment.setPaymentDate(LocalDate.now());

        paymentService.save(payment);


        redirectAttributes.addFlashAttribute("success", "Thanh toán thành công!");
        return "redirect:/myTicket";


    }


    @GetMapping("/payment/success")
    public String showSuccessPage(@RequestParam("ticketId") Long ticketId, Model model, HttpSession session) {
        Member member = (Member) session.getAttribute("member");
        if (member == null) {
            return "redirect:/login";
        }

        Ticket ticket = ticketService.findById(ticketId);
        if (ticket == null || !ticket.getMember().getMemberId().equals(member.getMemberId())) {
            return "redirect:/ticket";
        }

        model.addAttribute("ticket", ticket);
        return "member/success"; // ← file success.html trong templates/member
    }






}
