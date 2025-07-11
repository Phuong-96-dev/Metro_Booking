package sum25.se180455.metro.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import sum25.se180455.metro.pojo.Member;
import sum25.se180455.metro.pojo.Payment;
import sum25.se180455.metro.pojo.Ticket;
import sum25.se180455.metro.repository.MemberRepository;
import sum25.se180455.metro.repository.PaymentRepository;
import sum25.se180455.metro.repository.TicketRepository;
import sum25.se180455.metro.request.PaymentRequest;
import sum25.se180455.metro.service.BlogService;
import sum25.se180455.metro.service.MemberService;
import sum25.se180455.metro.service.PaymentService;
import sum25.se180455.metro.service.TicketService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
public class UserController {
    @Autowired
    private MemberService memberService;
    @Autowired
    private BlogService blogService;
    @Autowired
    private TicketService ticketService;
    @Autowired
    private PaymentService paymentService;

    // profile
    @GetMapping("/profile")
    public String profile() {
        return "profile";
    }

    // update profile
    @PostMapping("/update-profile")
    public String updateProfile(@ModelAttribute("member") Member member) {
        Member existingMember = memberService.findById(member.getMemberId());
        if (existingMember == null) {
            // Xử lý lỗi, không tìm thấy member
            return "redirect:/profile?error=Member not found";
        }

        // Chỉ update các trường cho phép
        existingMember.setName(member.getName());
        existingMember.setEmail(member.getEmail());
        existingMember.setPhone(member.getPhone());
        existingMember.setStudentCard(member.getStudentCard());
        existingMember.setSchoolName(member.getSchoolName());

        memberService.updateMember(existingMember);

        return "redirect:/profile?message=Profile updated successfully";
    }
    @GetMapping("/admindashboard")
    public String adminDashboard(Model model) {
        int memberCount = memberService.countMembersByRoleAndStatus("MEMBER", "ACTIVE");
        model.addAttribute("memberCount", memberCount);
        int blogCount = blogService.countBlogs();
        model.addAttribute("blogCount", blogCount);
        return "admindashboard";
    }
    // Admin dashboard
    @GetMapping("/adminManage/managerMember")
    public String adminManageMember(Model model) {
        List<Member> members = memberService.findAll();
        model.addAttribute("members", members);
        return "adminManage/manageMember";
    }

    // Update member status
    @PostMapping("/adminManage/updateMemberStatus")
    public String updateMemberStatus(@ModelAttribute("member") Member member) {
        Member existingMember = memberService.findById(member.getMemberId());
        if (existingMember == null) {
            // Xử lý lỗi, không tìm thấy member
            return "redirect:/adminManage/managerMember?error=Member not found";
        }

        existingMember.setStatus(member.getStatus());
        memberService.updateStatus(existingMember.getMemberId(), existingMember.getStatus());

        return "redirect:/adminManage/managerMember?message=Member status updated successfully";
    }
    // Update member role
    @PostMapping("/adminManage/updateMemberRole")
    public String updateMemberRole(@ModelAttribute("member") Member member) {
        Member existingMember = memberService.findById(member.getMemberId());
        if (existingMember == null) {
            // Xử lý lỗi, không tìm thấy member
            return "redirect:/adminManage/managerMember?error=Member not found";
        }

        existingMember.setRole(member.getRole());
        memberService.updateRole(existingMember.getMemberId(), existingMember.getRole());

        return "redirect:/adminManage/managerMember?message=Member role updated successfully";
    }

    @GetMapping("/myTicket")
    public String showMyTickets(HttpSession session, Model model) {
        Member member = (Member) session.getAttribute("member");

        if (member == null) {
            return "redirect:/login";
        }

        Long memberId = member.getMemberId();
//        String status = "Hoạt động";

        // Lấy tất cả vé ACTIVE của member
        List<Ticket> activeTickets = ticketService.findByMember_MemberId(memberId);

        // Kiểm tra hạn vé
        LocalDateTime now = LocalDateTime.now();
        List<Ticket> expiredTickets = new ArrayList<>();

        for (Ticket ticket : activeTickets) {
            if (ticket.getDate_end() != null && ticket.getDate_end().isBefore(now)) {
                ticket.setStatus("Hết hạn");
                ticketService.save(ticket); // Cập nhật trạng thái trong DB
                expiredTickets.add(ticket);
            }
        }


        model.addAttribute("tickets", activeTickets);
        return "member/myTicket";
    }



}
