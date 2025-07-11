package sum25.se180455.metro.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import sum25.se180455.metro.pojo.Blog;
import sum25.se180455.metro.pojo.Feedback;
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
        return "member/send_feedback"; // Trang form gửi feedback
    }

    @PostMapping("/feedback/send")
    public String sendFeedback(@RequestParam String title,
                               @RequestParam String content,
                               @RequestParam double rating) {

        Feedback feedback = new Feedback();
        feedback.setTitle(title);
        feedback.setContent(content);
        feedback.setRating(rating);
        feedback.setPostingDate(LocalDate.now());

        feedbackService.createFeedback(feedback);

        return "redirect:/myTicket?success";
    }



}

