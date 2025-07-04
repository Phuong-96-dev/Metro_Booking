package sum25.se180455.metro.pojo;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;

@Entity
public class TicketType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ticketTypeId;

    private String name;
    private Double price;
    private LocalDate dateStart;
    private LocalDate expirationDate;

    @OneToMany(mappedBy = "ticketType")
    private List<Ticket> tickets;

    @ManyToOne
    @JoinColumn(name = "station_start_id")
    private TrainStation stationStart;

    @ManyToOne
    @JoinColumn(name = "station_end_id")
    private TrainStation stationEnd;

    public TicketType(Long ticketTypeId, String name, Double price, LocalDate dateStart, LocalDate expirationDate, List<Ticket> tickets, TrainStation stationStart, TrainStation stationEnd) {
        this.ticketTypeId = ticketTypeId;
        this.name = name;
        this.price = price;
        this.dateStart = dateStart;
        this.expirationDate = expirationDate;
        this.tickets = tickets;
        this.stationStart = stationStart;
        this.stationEnd = stationEnd;
    }

    public TicketType(String name, Double price, LocalDate dateStart, LocalDate expirationDate, List<Ticket> tickets, TrainStation stationStart, TrainStation stationEnd) {
        this.name = name;
        this.price = price;
        this.dateStart = dateStart;
        this.expirationDate = expirationDate;
        this.tickets = tickets;
        this.stationStart = stationStart;
        this.stationEnd = stationEnd;
    }

    public TicketType() {
    }

    public Long getTicketTypeId() {
        return ticketTypeId;
    }

    public void setTicketTypeId(Long ticketTypeId) {
        this.ticketTypeId = ticketTypeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public LocalDate getDateStart() {
        return dateStart;
    }

    public void setDateStart(LocalDate dateStart) {
        this.dateStart = dateStart;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public List<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
    }

    public TrainStation getStationStart() {
        return stationStart;
    }

    public void setStationStart(TrainStation stationStart) {
        this.stationStart = stationStart;
    }

    public TrainStation getStationEnd() {
        return stationEnd;
    }

    public void setStationEnd(TrainStation stationEnd) {
        this.stationEnd = stationEnd;
    }
}

