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
    public Train() {
    }
}

