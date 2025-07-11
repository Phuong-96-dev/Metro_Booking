package sum25.se180455.metro.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import sum25.se180455.metro.pojo.Feedback;
import sum25.se180455.metro.pojo.Member;
import sum25.se180455.metro.repository.FeedbackRepository;
import sum25.se180455.metro.service.MemberService;
import sum25.se180455.metro.service.TrainStationService;

import java.util.List;

@Controller
public class HomeController {
    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private MemberService memberService;

    @Autowired
    private TrainStationService trainStationService;

    @GetMapping("/")
    public String homePage(Model model) {
        // Lấy danh sách ga tàu
        model.addAttribute("stations", trainStationService.getAllTrainStations());

        // Lấy danh sách feedback
        List<Feedback> feedbackList = feedbackRepository.findAll();

        double averageRating = feedbackList.stream()
                .mapToDouble(Feedback::getRating)
                .average()
                .orElse(0.0);

        model.addAttribute("feedbacks", feedbackList);
        model.addAttribute("averageRating", averageRating);
        model.addAttribute("totalFeedback", feedbackList.size());

        return "index"; // hoặc "index" tùy vào file template của bạn
    }


    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(HttpSession session,
                        @RequestParam String email,
                        @RequestParam String password) {
        Member member = memberService.login(email, password);
        if (member != null) {
            if (!"ACTIVE".equalsIgnoreCase(member.getStatus())) {
                return "redirect:/login?message=Account not available";
            }

            session.setAttribute("member", member);

            if ("ADMIN".equalsIgnoreCase(member.getRole())) {
                return "redirect:/admindashboard?message=Welcome admin";
            } else {
                return "redirect:/?message=Login successful";
            }
        } else {
            return "redirect:/login?message=Invalid email or password";
        }
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        Member newMember = new Member();
        newMember.setRole("MEMBER");
        model.addAttribute("newMember", newMember);
        return "register";
    }

    @PostMapping("/register")
    public String processRegister(@ModelAttribute("newMember") Member newMember) {
        if (newMember == null) {
            System.out.println("Member is null. Possibly form binding failed.");
            return "redirect:/register?error=Member is null";
        }

        System.out.println("Member info:");
        System.out.println("Name: " + newMember.getName());
        System.out.println("Email: " + newMember.getEmail());
        System.out.println("Phone: " + newMember.getPhone());
        System.out.println("Password: " + newMember.getPassword());

        newMember.setRole("MEMBER");
        memberService.register(newMember);
        return "redirect:/login?message=Registration successful, please login";
    }


//    @GetMapping("/ticket")
//    public String ticketPage() {
//        return "ticket";
//    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/?message=Logged out";
    }
    // more
    @GetMapping("/more")
    public String morePage() {
        return "more";
    }

}