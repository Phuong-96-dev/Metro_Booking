package sum25.se180455.metro.pojo;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Date;

@Entity
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    private LocalDate paymentDate;
    private Double price;
    private String paymentMethod;
    private String status;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    public Payment(Long paymentId, LocalDate paymentDate, Double price, String status, String paymentMethod, Member member) {
        this.paymentId = paymentId;
        this.paymentDate = paymentDate;
        this.price = price;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.member = member;
    }

    public Payment(LocalDate paymentDate, Double price, String paymentMethod, String status, Member member) {
        this.paymentDate = paymentDate;
        this.price = price;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.member = member;
    }
    public Payment() {
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }
}
