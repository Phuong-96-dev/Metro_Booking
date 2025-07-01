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

    public TrainSchedule(Long scheduleId, LocalDate startTime, LocalDate departTime, LocalDate arrivalTime, String stationStart, String stationEnd, Train train, List<Ticket> tickets, List<TrainStation> stations) {
        this.scheduleId = scheduleId;
        this.startTime = startTime;
        this.departTime = departTime;
        this.arrivalTime = arrivalTime;
        this.stationStart = stationStart;
        this.stationEnd = stationEnd;
        this.train = train;
        this.tickets = tickets;
        this.stations = stations;
    }

    public TrainSchedule(LocalDate startTime, LocalDate departTime, LocalDate arrivalTime, String stationStart, String stationEnd, Train train, List<Ticket> tickets, List<TrainStation> stations) {
        this.startTime = startTime;
        this.departTime = departTime;
        this.arrivalTime = arrivalTime;
        this.stationStart = stationStart;
        this.stationEnd = stationEnd;
        this.train = train;
        this.tickets = tickets;
        this.stations = stations;
    }
    public TrainSchedule() {
    }

    public Long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Long scheduleId) {
        this.scheduleId = scheduleId;
    }

    public LocalDate getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDate startTime) {
        this.startTime = startTime;
    }

    public LocalDate getDepartTime() {
        return departTime;
    }

    public void setDepartTime(LocalDate departTime) {
        this.departTime = departTime;
    }

    public LocalDate getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalDate arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public String getStationStart() {
        return stationStart;
    }

    public void setStationStart(String stationStart) {
        this.stationStart = stationStart;
    }

    public String getStationEnd() {
        return stationEnd;
    }

    public void setStationEnd(String stationEnd) {
        this.stationEnd = stationEnd;
    }

    public Train getTrain() {
        return train;
    }

    public void setTrain(Train train) {
        this.train = train;
    }

    public List<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
    }

    public List<TrainStation> getStations() {
        return stations;
    }

    public void setStations(List<TrainStation> stations) {
        this.stations = stations;
    }
}

