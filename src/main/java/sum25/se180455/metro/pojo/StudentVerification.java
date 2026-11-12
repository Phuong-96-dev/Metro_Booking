package sum25.se180455.metro.pojo;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_verification")
public class StudentVerification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_id")
    private Member reviewedBy;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false, length = 500)
    private String imagePath;

    @Column(nullable = false, length = 100)
    private String imageContentType;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String ocrText;

    @Column(length = 150)
    private String ocrFullName;

    @Column(length = 100)
    private String ocrStudentCode;

    @Column(length = 200)
    private String ocrSchoolName;

    private LocalDate ocrDateOfBirth;
    private LocalDate ocrIssuedAt;
    private LocalDate ocrExpiresAt;

    @Column(nullable = false, length = 150)
    private String fullName;

    @Column(nullable = false, length = 100)
    private String studentCode;

    @Column(nullable = false, length = 200)
    private String schoolName;

    private LocalDate dateOfBirth;
    private LocalDate issuedAt;
    private LocalDate expiresAt;

    @Column(length = 1000)
    private String rejectionReason;

    private LocalDateTime createdAt;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Member getMember() { return member; }
    public void setMember(Member member) { this.member = member; }
    public Member getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(Member reviewedBy) { this.reviewedBy = reviewedBy; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public String getImageContentType() { return imageContentType; }
    public void setImageContentType(String imageContentType) { this.imageContentType = imageContentType; }
    public String getOcrText() { return ocrText; }
    public void setOcrText(String ocrText) { this.ocrText = ocrText; }
    public String getOcrFullName() { return ocrFullName; }
    public void setOcrFullName(String ocrFullName) { this.ocrFullName = ocrFullName; }
    public String getOcrStudentCode() { return ocrStudentCode; }
    public void setOcrStudentCode(String ocrStudentCode) { this.ocrStudentCode = ocrStudentCode; }
    public String getOcrSchoolName() { return ocrSchoolName; }
    public void setOcrSchoolName(String ocrSchoolName) { this.ocrSchoolName = ocrSchoolName; }
    public LocalDate getOcrDateOfBirth() { return ocrDateOfBirth; }
    public void setOcrDateOfBirth(LocalDate ocrDateOfBirth) { this.ocrDateOfBirth = ocrDateOfBirth; }
    public LocalDate getOcrIssuedAt() { return ocrIssuedAt; }
    public void setOcrIssuedAt(LocalDate ocrIssuedAt) { this.ocrIssuedAt = ocrIssuedAt; }
    public LocalDate getOcrExpiresAt() { return ocrExpiresAt; }
    public void setOcrExpiresAt(LocalDate ocrExpiresAt) { this.ocrExpiresAt = ocrExpiresAt; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }
    public String getSchoolName() { return schoolName; }
    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public LocalDate getIssuedAt() { return issuedAt; }
    public void setIssuedAt(LocalDate issuedAt) { this.issuedAt = issuedAt; }
    public LocalDate getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDate expiresAt) { this.expiresAt = expiresAt; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
}
