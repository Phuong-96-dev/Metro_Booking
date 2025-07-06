package sum25.se180455.metro.pojo;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Train {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long trainId;

    private String name;

    private String status;

    private int capacity;

    @OneToMany(mappedBy = "train")
    private List<TrainSchedule> schedules;

    public Train() {
    }

    public Train(Long trainId, String name, String status, int capacity, List<TrainSchedule> schedules) {
        this.trainId = trainId;
        this.name = name;
        this.status = status;
        this.capacity = capacity;
        this.schedules = schedules;
    }

    public Train(String name, String status, int capacity, List<TrainSchedule> schedules) {
        this.name = name;
        this.status = status;
        this.capacity = capacity;
        this.schedules = schedules;
    }

    public Long getTrainId() {
        return trainId;
    }

    public void setTrainId(Long trainId) {
        this.trainId = trainId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public List<TrainSchedule> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<TrainSchedule> schedules) {
        this.schedules = schedules;
    }
}

