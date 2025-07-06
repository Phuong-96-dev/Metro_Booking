package sum25.se180455.metro.pojo;

import jakarta.persistence.*;

import java.time.LocalDate;
@Entity
public class Blog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long blogId;

    private String title;
    private String content;
    private String imageUrl;
    private LocalDate postingDate;

    @ManyToOne
    @JoinColumn(name = "admin_id") // đặt tên cột rõ nghĩa
    private Member admin;

    public Blog() {}
    public Blog(Long blogId, String title, String content, String imageUrl, LocalDate postingDate, Member admin) {
        this.blogId = blogId;
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
        this.postingDate = postingDate;
        this.admin = admin;
    }

    public Blog(String title, String content, String imageUrl, LocalDate postingDate, Member admin) {
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
        this.postingDate = postingDate;
        this.admin = admin;
    }

    public Blog(String title, String content, String imageUrl, Member admin) {
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
        this.admin = admin;
    }

    public Long getBlogId() {
        return blogId;
    }

    public void setBlogId(Long blogId) {
        this.blogId = blogId;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public LocalDate getPostingDate() {
        return postingDate;
    }

    public void setPostingDate(LocalDate postingDate) {
        this.postingDate = postingDate;
    }

    public Member getAdmin() {
        return admin;
    }

    public void setAdmin(Member admin) {
        this.admin = admin;
    }
}

