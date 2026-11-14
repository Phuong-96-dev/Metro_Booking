package sum25.se180455.metro.pojo;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    private LocalDate paymentDate;
    private Double price;
    private String paymentMethod;
    @Column(name = "status", columnDefinition = "NVARCHAR(255)")
    private String status;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToOne
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @Column(name = "transaction_code", length = 64)
    private String transactionCode;

    @Column(name = "sepay_transaction_id")
    private Long sepayTransactionId;

    @Column(name = "payment_content", columnDefinition = "NVARCHAR(1000)")
    private String paymentContent;

    @Column(name = "bank_reference", length = 100)
    private String bankReference;

    @Column(name = "sepay_gateway", length = 50)
    private String sepayGateway;

    @Column(name = "transfer_amount")
    private Long transferAmount;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;
    public Payment() {
    }

    public Payment(Long paymentId, LocalDate paymentDate, Double price, String paymentMethod, String status, Member member, Ticket ticket) {
        this.paymentId = paymentId;
        this.paymentDate = paymentDate;
        this.price = price;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.member = member;
        this.ticket = ticket;
    }

    public Payment(LocalDate paymentDate, Double price, String paymentMethod, String status, Member member, Ticket ticket) {
        this.paymentDate = paymentDate;
        this.price = price;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.member = member;
        this.ticket = ticket;
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

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public String getTransactionCode() {
        return transactionCode;
    }

    public void setTransactionCode(String transactionCode) {
        this.transactionCode = transactionCode;
    }

    public Long getSepayTransactionId() {
        return sepayTransactionId;
    }

    public void setSepayTransactionId(Long sepayTransactionId) {
        this.sepayTransactionId = sepayTransactionId;
    }

    public String getPaymentContent() {
        return paymentContent;
    }

    public void setPaymentContent(String paymentContent) {
        this.paymentContent = paymentContent;
    }

    public String getBankReference() {
        return bankReference;
    }

    public void setBankReference(String bankReference) {
        this.bankReference = bankReference;
    }

    public String getSepayGateway() {
        return sepayGateway;
    }

    public void setSepayGateway(String sepayGateway) {
        this.sepayGateway = sepayGateway;
    }

    public Long getTransferAmount() {
        return transferAmount;
    }

    public void setTransferAmount(Long transferAmount) {
        this.transferAmount = transferAmount;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }
}
