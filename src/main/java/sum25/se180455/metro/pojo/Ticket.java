package sum25.se180455.metro.pojo;

import jakarta.persistence.*;

@Entity
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ticketId;

    private String status;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "schedule_id")
    private TrainSchedule schedule;

    @ManyToOne
    @JoinColumn(name = "ticket_type_id")
    private TicketType ticketType;

    public Ticket(Long ticketId, String status, Member member, TrainSchedule schedule, TicketType ticketType) {
        this.ticketId = ticketId;
        this.status = status;
        this.member = member;
        this.schedule = schedule;
        this.ticketType = ticketType;
    }

    public Ticket(String status, Member member, TrainSchedule schedule, TicketType ticketType) {
        this.status = status;
        this.member = member;
        this.schedule = schedule;
        this.ticketType = ticketType;
    }
    public Ticket() {
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
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

    public TrainSchedule getSchedule() {
        return schedule;
    }

    public void setSchedule(TrainSchedule schedule) {
        this.schedule = schedule;
    }

    public TicketType getTicketType() {
        return ticketType;
    }

    public void setTicketType(TicketType ticketType) {
        this.ticketType = ticketType;
    }
}

