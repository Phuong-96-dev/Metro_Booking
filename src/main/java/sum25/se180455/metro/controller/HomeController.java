package sum25.se180455.metro.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import sum25.se180455.metro.pojo.Member;
import sum25.se180455.metro.service.MemberService;

@Controller
public class HomeController {
    @Autowired
    private MemberService memberService;

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
}
