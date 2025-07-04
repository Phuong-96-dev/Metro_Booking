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

    public TrainStation(Long stationId, String name, LocalDate arrivalTime, String note, List<TrainSchedule> schedules, List<BusRoute> busRoutes) {
        this.stationId = stationId;
        this.name = name;
        this.arrivalTime = arrivalTime;
        this.note = note;
        this.schedules = schedules;
        this.busRoutes = busRoutes;
    }

    public TrainStation(String name, LocalDate arrivalTime, String note, List<TrainSchedule> schedules, List<BusRoute> busRoutes) {
        this.name = name;
        this.arrivalTime = arrivalTime;
        this.note = note;
        this.schedules = schedules;
        this.busRoutes = busRoutes;
    }
    public TrainStation() {
    }

    public Long getStationId() {
        return stationId;
    }

    public void setStationId(Long stationId) {
        this.stationId = stationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalDate arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public List<TrainSchedule> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<TrainSchedule> schedules) {
        this.schedules = schedules;
    }

    public List<BusRoute> getBusRoutes() {
        return busRoutes;
    }

    public void setBusRoutes(List<BusRoute> busRoutes) {
        this.busRoutes = busRoutes;
    }
}

