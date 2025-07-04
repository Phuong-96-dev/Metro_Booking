package sum25.se180455.metro.pojo;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Date;

@Entity
public class Blog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long blogId;

    private String title;
    private String content;
    private LocalDate postingDate;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "approved_by")
    private Admin approvedBy;
    public Blog() {
    }

    public Blog(String title, LocalDate postingDate, String content, Member member, Admin approvedBy) {
        this.title = title;
        this.postingDate = postingDate;
        this.content = content;
        this.member = member;
        this.approvedBy = approvedBy;
    }

    public Blog(Long blogId, String title, String content, LocalDate postingDate, Member member, Admin approvedBy) {
        this.blogId = blogId;
        this.title = title;
        this.content = content;
        this.postingDate = postingDate;
        this.member = member;
        this.approvedBy = approvedBy;
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

    public LocalDate getPostingDate() {
        return postingDate;
    }

    public void setPostingDate(LocalDate postingDate) {
        this.postingDate = postingDate;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public Admin getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(Admin approvedBy) {
        this.approvedBy = approvedBy;
    }
}

