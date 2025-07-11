package sum25.se180455.metro.pojo;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ticketId;
    @Column(name = "status", columnDefinition = "NVARCHAR(255)")
    private String status;

    private LocalDateTime date_start;

    private LocalDateTime date_end;

    private Double price;



    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "schedule_id")
    private TrainSchedule schedule;

    @ManyToOne
    @JoinColumn(name = "ticket_type_id")
    private TicketType ticketType;

    public Ticket() {
    }

    public Ticket(Long ticketId, String status, LocalDateTime date_start, LocalDateTime date_end, Double price, Member member, TrainSchedule schedule, TicketType ticketType) {
        this.ticketId = ticketId;
        this.status = status;
        this.date_start = date_start;
        this.date_end = date_end;
        this.price = price;
        this.member = member;
        this.schedule = schedule;
        this.ticketType = ticketType;
    }

    public Ticket(String status, LocalDateTime date_start, LocalDateTime date_end, Double price, Member member, TrainSchedule schedule, TicketType ticketType) {
        this.status = status;
        this.date_start = date_start;
        this.date_end = date_end;
        this.price = price;
        this.member = member;
        this.schedule = schedule;
        this.ticketType = ticketType;
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

    public LocalDateTime getDate_start() {
        return date_start;
    }

    public void setDate_start(LocalDateTime date_start) {
        this.date_start = date_start;
    }

    public LocalDateTime getDate_end() {
        return date_end;
    }

    public void setDate_end(LocalDateTime date_end) {
        this.date_end = date_end;
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

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }


}

