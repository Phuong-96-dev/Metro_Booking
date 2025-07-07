package sum25.se180455.metro.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sum25.se180455.metro.pojo.TrainSchedule;
import sum25.se180455.metro.repository.TrainScheduleRepository;

import java.util.List;
import java.util.Optional;

@Service
public class TrainScheduleServiceImpl implements  TrainScheduleService {
    @Autowired
    private TrainScheduleRepository trainScheduleRepository;

    @Override
    public List<TrainSchedule> getAllTrainSchedules() {
        return trainScheduleRepository.findAll();
    }
    @Override
    public Optional<TrainSchedule> getTrainScheduleById(Long id) {
        return trainScheduleRepository.findById(id);
    }
    @Override
    public TrainSchedule saveTrainSchedule(TrainSchedule schedule) {
        return trainScheduleRepository.save(schedule);
    }
    @Override
    public void deleteTrainSchedule(Long id) {
        trainScheduleRepository.deleteById(id);
    }
}