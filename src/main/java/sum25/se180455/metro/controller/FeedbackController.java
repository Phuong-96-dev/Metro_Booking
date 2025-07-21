package sum25.se180455.metro.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import sum25.se180455.metro.pojo.Blog;
import sum25.se180455.metro.pojo.Feedback;
import sum25.se180455.metro.pojo.Member;
import sum25.se180455.metro.repository.FeedbackRepository;
import sum25.se180455.metro.service.FeedbackService;

import java.time.LocalDate;
import java.util.List;

@Controller
public class FeedbackController {
    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private FeedbackService feedbackService;

    @GetMapping("/feedback")
    public String feedbackPage(Model model) {
        model.addAttribute("feedbackList", feedbackService.findAllBlogsOrderByDateDesc());
        return "member/feedback"; // feedback.html
    }

    @GetMapping("/feedback/send")
    public String sendFeedbackForm() {
        return "member/send_feedback";
    }

    @PostMapping("/feedback/send")
    public String sendFeedback(@RequestParam String title,
                               @RequestParam String content,
                               @RequestParam double rating,
                               HttpSession session) {
        Member member = (Member) session.getAttribute("member");

        if (member == null) {
            return "redirect:/login?message=Please login to send feedback";
        }

        Feedback feedback = new Feedback();
        feedback.setTitle(title);
        feedback.setContent(content);
        feedback.setRating(rating);
        feedback.setPostingDate(LocalDate.now());
        feedback.setMember(member); // Gán member

        feedbackService.createFeedback(feedback);

        return "redirect:/myTicket?success";
    }

    @GetMapping("/feedback/all")
    public String viewAllFeedbacks(Model model) {
        model.addAttribute("feedbackList", feedbackService.findAllFeedbacks());
        return "member/showFeedback";
    }


}
