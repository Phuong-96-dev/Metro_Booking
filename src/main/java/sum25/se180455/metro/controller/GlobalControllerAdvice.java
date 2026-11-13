package sum25.se180455.metro.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import sum25.se180455.metro.pojo.Member;

@ControllerAdvice
public class GlobalControllerAdvice {

    @ModelAttribute
    public void addMemberToModel(HttpSession session, Model model) {
        Member member = (Member) session.getAttribute("member");
        model.addAttribute("member", member);
        model.addAttribute("isAdmin", member != null && "ADMIN".equalsIgnoreCase(member.getRole()));
    }
}
