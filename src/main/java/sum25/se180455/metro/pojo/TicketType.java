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
    private String description;

    @OneToMany(mappedBy = "ticketType")
    private List<Ticket> tickets;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "train_station_id")
    private TrainStation trainStation;

    public TicketType() {
    }

    public TicketType(Long ticketTypeId, String name, Double price, String description, List<Ticket> tickets, TrainStation trainStation) {
        this.ticketTypeId = ticketTypeId;
        this.name = name;
        this.price = price;
        this.description = description;
        this.tickets = tickets;
        this.trainStation = trainStation;
    }

    public TicketType(String name, Double price, String description, List<Ticket> tickets, TrainStation trainStation) {
        this.name = name;
        this.price = price;
        this.description = description;
        this.tickets = tickets;
        this.trainStation = trainStation;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
    }

    public TrainStation getTrainStation() {
        return trainStation;
    }

    public void setTrainStation(TrainStation trainStation) {
        this.trainStation = trainStation;
    }
}

