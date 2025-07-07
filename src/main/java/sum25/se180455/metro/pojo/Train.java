package sum25.se180455.metro.pojo;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Train {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long trainId;

    private String name;

    @ManyToOne
    @JoinColumn(name = "admin_id")
    private Admin admin;

    @OneToMany(mappedBy = "train")
    private List<TrainSchedule> schedules;

    public Train() {
    }

    public Train(Long trainId, String name, Admin admin, List<TrainSchedule> schedules) {
        this.trainId = trainId;
        this.name = name;
        this.admin = admin;
        this.schedules = schedules;
    }

    public Train(String name, Admin admin, List<TrainSchedule> schedules) {
        this.name = name;
        this.admin = admin;
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

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public List<TrainSchedule> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<TrainSchedule> schedules) {
        this.schedules = schedules;
    }
}

