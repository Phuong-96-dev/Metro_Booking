package sum25.se180455.metro.request;

public class PaymentRequest {
    private Long ticketId;
    private Long memberId;
    private String fullName;
    private String email;
    private String phone;
    private String paymentMethod;
    private String note;

    public PaymentRequest() {
    }

    public PaymentRequest(Long ticketId, Long memberId, String fullName, String email,
                          String phone, String paymentMethod, String note) {
        this.ticketId = ticketId;
        this.memberId = memberId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.paymentMethod = paymentMethod;
        this.note = note;
    }

    // Getter và Setter
    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
