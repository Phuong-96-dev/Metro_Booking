package sum25.se180455.metro.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sum25.se180455.metro.pojo.TrainSchedule;
import sum25.se180455.metro.repository.TrainScheduleRepository;

import java.util.List;

@Service
public class TrainScheduleServiceImpl implements TrainScheduleService {
    @Autowired
    private TrainScheduleRepository trainScheduleRepository;

    @Override
    public TrainSchedule getTrainScheduleById(Long id) {
        return trainScheduleRepository.getTrainScheduleByScheduleId(id);
    }

    @Override
    public List<TrainSchedule> getTrainSchedules() {
        return List.of();
    }
}
