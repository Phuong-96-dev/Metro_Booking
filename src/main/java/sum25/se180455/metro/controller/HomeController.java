package sum25.se180455.metro.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import sum25.se180455.metro.pojo.Member;
import sum25.se180455.metro.pojo.Ticket;
import sum25.se180455.metro.pojo.TicketType;
import sum25.se180455.metro.pojo.TrainSchedule;
import sum25.se180455.metro.service.MemberService;
import sum25.se180455.metro.service.TicketService;
import sum25.se180455.metro.service.TicketTypeService;
import sum25.se180455.metro.service.TrainScheduleService;

import java.util.List;

@Controller
public class HomeController {
    @Autowired
    private MemberService memberService;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private TicketTypeService ticketTypeService;

    @Autowired
    private TrainScheduleService trainScheduleService;

    @GetMapping("/login")
    public String loginPage() {
        return "/login"; // trả về login.html
    }

    @PostMapping("/login")
    public String login(HttpSession session, @RequestParam String email, @RequestParam String password) {
        Member member = memberService.login(email, password);
        if (member != null) {
            session.setAttribute("member", member);
            // muons chuyển hươớng đi âu thì ở dđây
            return "redirect:/?message=Login successful";
        } else {
            return "redirect:/login?message=Invalid email or password";
        }
    }
    @GetMapping("/register")
    public String registerPage(Model model) {
        Member member = new Member();
        member.setRole("MEMBER"); // mặc định role
        model.addAttribute("member", member);
        return "register"; // trả về register.html
    }

    @PostMapping("/register")
    public String processRegister(@ModelAttribute Member member) {
        member.setRole("MEMBER"); // luôn gán role là MEMBER
         memberService.register(member);
        return "redirect:/success";
    }

    @GetMapping("/ticket")
    public String showAllTicketTypes(Model model) {
        List<TicketType> types = ticketTypeService.getTicketTypes();
        model.addAttribute("ticketTypes", types);

        return "ticket"; // lúc này ticket.html sẽ hiển thị ticketTypes
    }

    @PostMapping("/ticket/buy")
    public String buyTicket(@RequestParam("ticketTypeId") Long ticketTypeId,
                            @RequestParam(value = "scheduleId", required = false) Long scheduleId,
                            HttpSession session,
                            Model model) {
        // Lấy member từ session (phải đăng nhập mới mua vé được)
        Member member = (Member) session.getAttribute("member");
        if (member == null) {
            return "redirect:/login";

        }

        // Lấy loại vé
        TicketType ticketType = ticketTypeService.getTicketTypeById(ticketTypeId);
        if (ticketType == null) {
            return "redirect:/ticket?message=Không tìm thấy loại vé";
        }

        // Vì một số loại vé không cần schedule, nên kiểm tra kỹ
        Ticket ticket;
        if (scheduleId != null) {
            // Nếu có schedule thì cần viết thêm `trainScheduleService`
            TrainSchedule schedule = trainScheduleService.getTrainScheduleById(scheduleId);
            if (schedule == null) {
                return "redirect:/ticket?message=Không tìm thấy lịch trình";
            }

            ticket = new Ticket("Đã mua", member, schedule, ticketType);
        } else {
            // Nếu không có schedule
            ticket = new Ticket("Đã mua", member, null, ticketType);
        }

        ticketService.save(ticket);
        return "redirect:/ticket?message=Mua vé thành công";
    }


}
