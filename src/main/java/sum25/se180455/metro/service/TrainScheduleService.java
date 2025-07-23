package sum25.se180455.metro.service;

import sum25.se180455.metro.pojo.TrainSchedule;
import sum25.se180455.metro.pojo.TrainStation;
import sum25.se180455.metro.request.ScheduleResult;

import java.util.List;

import java.util.Optional;

public interface TrainScheduleService {
    public List<TrainSchedule> getAllTrainSchedules();
    public Optional<TrainSchedule> getTrainScheduleById(Long id);
    public TrainSchedule saveTrainSchedule(TrainSchedule schedule);
    public void deleteTrainSchedule(Long id);
    List<ScheduleResult> searchSchedules(Long startStationId, Long endStationId);
    List<TrainStation> getTerminalStations();
    TrainSchedule validateAndSaveSchedule(TrainSchedule schedule, Long startStationId, Long endStationId);
}
