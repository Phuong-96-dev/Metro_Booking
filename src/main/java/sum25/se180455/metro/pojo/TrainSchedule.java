package sum25.se180455.metro.pojo;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;

@Entity
public class TrainSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scheduleId;

    private LocalDate startTime;
    private LocalDate departTime;
    private LocalDate arrivalTime;
    private String stationStart;
    private String stationEnd;

    @ManyToOne
    @JoinColumn(name = "train_id")
    private Train train;

    @OneToMany(mappedBy = "schedule")
    private List<Ticket> tickets;

    @ManyToMany
    @JoinTable(
            name = "schedule_station",
            joinColumns = @JoinColumn(name = "schedule_id"),
            inverseJoinColumns = @JoinColumn(name = "station_id")
    )
    private List<TrainStation> stations;
}

