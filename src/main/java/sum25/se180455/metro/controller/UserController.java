package sum25.se180455.metro.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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
import sum25.se180455.metro.service.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private BusRouteService busRouteService;
    @Autowired
    private RenueService revenueService;
    @Autowired
    private TrainStationService trainStationService;

    // profile
    @GetMapping("/profile")
    public String profile() {
        return "profile";
    }

    // update profile
    @PostMapping("/update-profile")
    public String updateProfile(@ModelAttribute("profileUpdate") Member member, HttpSession session) {
        Member currentMember = (Member) session.getAttribute("member");
        Member existingMember = memberService.findById(currentMember.getMemberId());
        if (existingMember == null) {
            // Xử lý lỗi, không tìm thấy member
            return "redirect:/profile?error=Member not found";
        }

        // Chỉ update các trường cho phép
        if (member.getName() == null || member.getName().isBlank() || member.getName().length() > 100
                || (member.getPhone() != null && !member.getPhone().matches("[+0-9 ()-]{0,20}"))
                || (member.getStudentCard() != null && member.getStudentCard().length() > 100)
                || (member.getSchoolName() != null && member.getSchoolName().length() > 200)) {
            return "redirect:/profile?invalid";
        }
        existingMember.setName(member.getName());
        // Email is a login identity; changing it requires a separate verification flow.
        existingMember.setPhone(member.getPhone());
        existingMember.setStudentCard(member.getStudentCard());
        existingMember.setSchoolName(member.getSchoolName());

        memberService.updateMember(existingMember);

        return "redirect:/profile?message=Profile updated successfully";
    }

    @GetMapping("/admindashboard")
    public String adminDashboard(
            @RequestParam(value = "type", defaultValue = "month") String type,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            Model model) {

        int memberCount = memberService.countMembersByRoleAndStatus("MEMBER", "ACTIVE");
        int blogCount = blogService.countBlogs();
        int busCount = busRouteService.countByBusStatus("ACTIVE");
        long trainStationCount = trainStationService.count();
        int count1 = ticketService.countByStatus("Chưa sử dụng");
        int count2 = ticketService.countByStatus("Đang sử dụng");
        int ticketCount = count1 + count2;

        model.addAttribute("memberCount", memberCount);
        model.addAttribute("blogCount", blogCount);
        model.addAttribute("busCount", busCount);
        model.addAttribute("trainStationCount", trainStationCount);
        model.addAttribute("ticketCount", ticketCount);

        double totalRevenue = revenueService.getTotalRevenue();
        model.addAttribute("totalRevenue", totalRevenue);

        Map<String, Double> revenueByType = revenueService.getRevenueByTicketType();
        model.addAttribute("ticketTypeNames", new ArrayList<>(revenueByType.keySet()));
        model.addAttribute("ticketTypeRevenues", new ArrayList<>(revenueByType.values()));

        Map<String, Double> revenueTimeMap;
        switch (type) {
            case "day":
                if (startDate != null && endDate != null) {
                    revenueTimeMap = revenueService.getDailyRevenue(startDate, endDate);
                } else {
                    // fallback: lấy 7 ngày gần nhất
                    LocalDate end = LocalDate.now();
                    LocalDate start = end.minusDays(6);
                    revenueTimeMap = revenueService.getDailyRevenue(start, end);
                }
                break;
            case "year":
                revenueTimeMap = revenueService.getYearlyRevenue();
                break;
            default:
                revenueTimeMap = revenueService.getMonthlyRevenueCurrentYear();
                break;
        }

        model.addAttribute("months", new ArrayList<>(revenueTimeMap.keySet()));
        model.addAttribute("monthlyRevenue", new ArrayList<>(revenueTimeMap.values()));
        model.addAttribute("selectedType", type);
        model.addAttribute("selectedStartDate", startDate);
        model.addAttribute("selectedEndDate", endDate);

        return "admindashboard";
    }
    // Admin dashboard
    @GetMapping("/adminManage/managerMember")
    public String manageMembers(Model model) {
        List<Member> members = memberService.findAll();

        Map<Long, Integer> memberTicketCounts = new HashMap<>();
        Map<Long, Double> memberTotalPaid = new HashMap<>();

        for (Member member : members) {
            List<Ticket> tickets = ticketService.getTicketsByMemberId(member.getMemberId());
            memberTicketCounts.put(member.getMemberId(), tickets.size());

            Double totalPaid = ticketService.getTotalPaidByMemberId(member.getMemberId());
            memberTotalPaid.put(member.getMemberId(), totalPaid != null ? totalPaid : 0.0);
        }

        model.addAttribute("members", members);
        model.addAttribute("memberTicketCounts", memberTicketCounts);
        model.addAttribute("memberTotalPaid", memberTotalPaid);

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
