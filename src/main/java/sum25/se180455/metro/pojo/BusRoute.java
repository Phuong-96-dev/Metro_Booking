package sum25.se180455.metro.pojo;

import jakarta.persistence.*;

@Entity
public class BusRoute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long busId;

    private String name;
    private String location;
    private String busStatus;

    @ManyToOne
    @JoinColumn(name = "station_id")
    private TrainStation station;
}

