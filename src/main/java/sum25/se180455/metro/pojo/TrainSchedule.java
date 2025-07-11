package sum25.se180455.metro.pojo;

import jakarta.persistence.*;
import java.time.LocalTime;
import java.util.List;

@Entity
public class TrainSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scheduleId;

    private LocalTime departTime;
    private LocalTime arrivalTime;

    @ManyToOne
    @JoinColumn(name = "start_station_id")
    private TrainStation startStation;

    @ManyToOne
    @JoinColumn(name = "end_station_id")
    private TrainStation endStation;

    @ManyToOne
    @JoinColumn(name = "train_id")
    private Train train;

    @OneToMany(mappedBy = "schedule")
    private List<Ticket> tickets;

    public TrainSchedule() {
    }

    public TrainSchedule(LocalTime departTime, LocalTime arrivalTime,
                         TrainStation startStation, TrainStation endStation, Train train, List<Ticket> tickets) {
        this.departTime = departTime;
        this.arrivalTime = arrivalTime;
        this.startStation = startStation;
        this.endStation = endStation;
        this.train = train;
        this.tickets = tickets;
    }

    // Getters and Setters
    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }
    public LocalTime getDepartTime() { return departTime; }
    public void setDepartTime(LocalTime departTime) { this.departTime = departTime; }
    public LocalTime getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(LocalTime arrivalTime) { this.arrivalTime = arrivalTime; }
    public TrainStation getStartStation() { return startStation; }
    public void setStartStation(TrainStation startStation) { this.startStation = startStation; }
    public TrainStation getEndStation() { return endStation; }
    public void setEndStation(TrainStation endStation) { this.endStation = endStation; }
    public Train getTrain() { return train; }
    public void setTrain(Train train) { this.train = train; }
    public List<Ticket> getTickets() { return tickets; }
    public void setTickets(List<Ticket> tickets) { this.tickets = tickets; }
}