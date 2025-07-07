package sum25.se180455.metro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sum25.se180455.metro.pojo.TrainSchedule;
@Repository

public interface TrainScheduleRepository extends JpaRepository<TrainSchedule,Long> {
    TrainSchedule findTrainScheduleByScheduleId(Long scheduleId);

    TrainSchedule getTrainScheduleByScheduleId(Long scheduleId);

}
