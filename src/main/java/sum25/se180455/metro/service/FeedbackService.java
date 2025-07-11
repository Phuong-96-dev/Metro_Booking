package sum25.se180455.metro.service;

import sum25.se180455.metro.pojo.Blog;
import sum25.se180455.metro.pojo.Feedback;

import java.util.List;

public interface FeedbackService {
    Feedback createFeedback(Feedback feedback);
    List<Feedback> findAllBlogsOrderByDateDesc();
}
