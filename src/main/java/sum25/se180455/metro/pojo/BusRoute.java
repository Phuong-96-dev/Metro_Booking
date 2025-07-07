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

    public BusRoute() {
    }

    public BusRoute(String name, String location, String busStatus, TrainStation station) {
        this.name = name;
        this.location = location;
        this.busStatus = busStatus;
        this.station = station;
    }

    public BusRoute(Long busId, String name, String location, String busStatus, TrainStation station) {
        this.busId = busId;
        this.name = name;
        this.location = location;
        this.busStatus = busStatus;
        this.station = station;
    }

    public Long getBusId() {
        return busId;
    }

    public void setBusId(Long busId) {
        this.busId = busId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBusStatus() {
        return busStatus;
    }

    public void setBusStatus(String busStatus) {
        this.busStatus = busStatus;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public TrainStation getStation() {
        return station;
    }

    public void setStation(TrainStation station) {
        this.station = station;
    }
}