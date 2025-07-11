package sum25.se180455.metro.pojo;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long feedbackId;

    private String title;
    private String content;
    private LocalDate postingDate;
    @Column(name = "rating")
    private Double rating;


    public Feedback() {
    }

    public Feedback(Long feedbackId, String title, String content, LocalDate postingDate, Double rating) {
        this.feedbackId = feedbackId;
        this.title = title;
        this.content = content;
        this.postingDate = postingDate;
        this.rating = rating;
    }

    public Feedback(String title, String content, LocalDate postingDate, Double rating) {
        this.title = title;
        this.content = content;
        this.postingDate = postingDate;
        this.rating = rating;
    }

    public Long getFeedbackId() {
        return feedbackId;
    }

    public void setFeedbackId(Long feedbackId) {
        this.feedbackId = feedbackId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDate getPostingDate() {
        return postingDate;
    }

    public void setPostingDate(LocalDate postingDate) {
        this.postingDate = postingDate;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }
}