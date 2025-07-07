package sum25.se180455.metro.pojo;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class BusRoute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long busId;

    @Column(name = "name", columnDefinition = "NVARCHAR(100)")
    private String name;

    @Column(name = "location", columnDefinition = "NVARCHAR(100)")
    private String location;

    @Column(name = "bus_status", columnDefinition = "NVARCHAR(50)")
    private String busStatus;

    @ManyToOne
    @JoinColumn(name = "station_id")
    private TrainStation station;

    @OneToMany(mappedBy = "busRoute")
    private List<BusRouteDestination> destinations;

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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getBusStatus() {
        return busStatus;
    }

    public void setBusStatus(String busStatus) {
        this.busStatus = busStatus;
    }

    public TrainStation getStation() {
        return station;
    }

    public void setStation(TrainStation station) {
        this.station = station;
    }

    public List<BusRouteDestination> getDestinations() {
        return destinations;
    }

    public void setDestinations(List<BusRouteDestination> destinations) {
        this.destinations = destinations;
    }
}

