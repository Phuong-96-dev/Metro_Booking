package sum25.se180455.metro.service;

import sum25.se180455.metro.pojo.TrainSchedule;

import java.util.List;

public interface TrainScheduleService {
    public TrainSchedule getTrainScheduleById(Long id);
    public List<TrainSchedule> getTrainSchedules();
}
