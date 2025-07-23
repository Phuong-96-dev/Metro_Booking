package sum25.se180455.metro.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sum25.se180455.metro.pojo.Feedback;
import sum25.se180455.metro.pojo.Member;
import sum25.se180455.metro.repository.FeedbackRepository;

import java.time.LocalDate;
import java.util.List;
@Service
public class FeedbackServiceImpl implements FeedbackService {
    @Autowired
    private FeedbackRepository feedbackRepository;

    @Override
    public Feedback createFeedback(Feedback feedback) {
        feedback.setPostingDate(LocalDate.now());
        return feedbackRepository.save(feedback);
    }

    @Override
    public List<Feedback> findAllBlogsOrderByDateDesc() {
        return feedbackRepository.findAllByOrderByPostingDateDesc();
    }

    @Override
    public List<Feedback> findAllFeedbacks() {
        return feedbackRepository.findAll(); // hoặc custom method nếu có
    }
}
