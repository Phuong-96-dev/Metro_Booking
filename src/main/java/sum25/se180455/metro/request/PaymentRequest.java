package sum25.se180455.metro.request;

public class PaymentRequest {
    private Long ticketId;
    private Long memberId;

    public PaymentRequest() {
    }

    public PaymentRequest(Long ticketId, Long memberId) {
        this.ticketId = ticketId;
        this.memberId = memberId;
    }

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
}
