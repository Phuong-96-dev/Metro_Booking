package sum25.se180455.metro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sum25.se180455.metro.pojo.Blog;
import sum25.se180455.metro.pojo.Feedback;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findAllByOrderByPostingDateDesc();
}
