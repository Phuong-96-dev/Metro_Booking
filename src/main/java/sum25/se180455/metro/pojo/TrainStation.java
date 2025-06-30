package sum25.se180455.metro.pojo;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;

@Entity
public class TrainStation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stationId;

    private String name;
    private LocalDate arrivalTime;
    private String note;

    @ManyToMany(mappedBy = "stations")
    private List<TrainSchedule> schedules;

    @OneToMany(mappedBy = "station")
    private List<BusRoute> busRoutes;
}

