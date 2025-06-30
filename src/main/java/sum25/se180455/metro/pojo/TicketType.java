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
}

