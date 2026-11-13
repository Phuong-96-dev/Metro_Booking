package sum25.se180455.metro.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import sum25.se180455.metro.pojo.Feedback;
import sum25.se180455.metro.repository.FeedbackRepository;
import sum25.se180455.metro.service.TrainStationService;

import java.util.List;

@Controller
public class HomeController {
    @Autowired
    private FeedbackRepository feedbackRepository;

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


    // more
    @GetMapping("/more")
    public String morePage() {
        return "more";
    }

}
