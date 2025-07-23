package sum25.se180455.metro.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sum25.se180455.metro.pojo.TicketType;
import sum25.se180455.metro.pojo.Train;
import sum25.se180455.metro.pojo.TrainSchedule;
import sum25.se180455.metro.pojo.TrainStation;
import sum25.se180455.metro.repository.TrainScheduleRepository;
import sum25.se180455.metro.request.ScheduleResult;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TrainScheduleServiceImpl implements  TrainScheduleService {
    @Autowired
    private TrainScheduleRepository trainScheduleRepository;

    @Autowired
    private TrainStationService trainStationService;

    @Autowired
    private TicketTypeService ticketTypeService;

    @Autowired
    private TrainService trainService;

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
    @Override
    public List<ScheduleResult> searchSchedules(Long startStationId, Long endStationId) {
        TrainStation startStation = trainStationService.getTrainStationById(startStationId).orElseThrow();
        TrainStation endStation = trainStationService.getTrainStationById(endStationId).orElseThrow();

        int travelMinutes = Math.abs(startStation.getPosition() - endStation.getPosition()) * 3;

        return trainScheduleRepository.findAll().stream()
                .filter(schedule -> isValidSchedule(schedule, startStation, endStation))
                .map(schedule -> buildScheduleResult(schedule, startStation, endStation, travelMinutes))
                .collect(Collectors.toList());
    }
    @Override
    public List<TrainStation> getTerminalStations() {
        List<TrainStation> result = new ArrayList<>();
        trainStationService.getTrainStationByName("Bến Thành").ifPresent(result::add);
        trainStationService.getTrainStationByName("Bến xe Suối Tiên").ifPresent(result::add);
        return result;
    }

    @Override
    public TrainSchedule validateAndSaveSchedule(TrainSchedule schedule, Long startStationId, Long endStationId) {
        Optional<TrainStation> startStationOpt = trainStationService.getTrainStationById(startStationId);
        Optional<TrainStation> endStationOpt = trainStationService.getTrainStationById(endStationId);

        if (startStationOpt.isEmpty() || endStationOpt.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy ga bắt đầu hoặc ga kết thúc.");
        }

        TrainStation startStation = startStationOpt.get();
        TrainStation endStation = endStationOpt.get();
        schedule.setStartStation(startStation);
        schedule.setEndStation(endStation);

        if (!isValidStationPair(startStation, endStation)) {
            throw new IllegalArgumentException("Cặp ga không hợp lệ. Phải là Bến Thành đến Bến xe Suối Tiên hoặc ngược lại.");
        }

        if (schedule.getDepartTime() == null) {
            throw new IllegalArgumentException("Vui lòng nhập giờ khởi hành.");
        }

        schedule.setArrivalTime(schedule.getDepartTime().plusMinutes(39));

        if (schedule.getTrain() == null || schedule.getTrain().getTrainId() == null) {
            throw new IllegalArgumentException("Vui lòng chọn tàu.");
        }

        Optional<Train> trainOpt = trainService.getTrainById(schedule.getTrain().getTrainId());
        if (trainOpt.isEmpty()) {
            throw new IllegalArgumentException("Tàu không hợp lệ.");
        }

        schedule.setTrain(trainOpt.get());
        return trainScheduleRepository.save(schedule);
    }

    private boolean isValidSchedule(TrainSchedule schedule, TrainStation startStation, TrainStation endStation) {
        boolean isForward = schedule.getStartStation().getName().equals("Bến Thành");
        int startPos = startStation.getPosition();
        int endPos = endStation.getPosition();

        if (isForward) {
            return startPos <= endPos &&
                    startPos >= schedule.getStartStation().getPosition() &&
                    endPos <= schedule.getEndStation().getPosition();
        } else {
            return startPos >= endPos &&
                    startPos <= schedule.getStartStation().getPosition() &&
                    endPos >= schedule.getEndStation().getPosition();
        }
    }

    private ScheduleResult buildScheduleResult(TrainSchedule schedule,
                                               TrainStation startStation,
                                               TrainStation endStation,
                                               long travelMinutes) {

        boolean isForward = schedule.getStartStation().getName().equals("Bến Thành");
        LocalTime departTime;
        LocalTime arrivalTime;

        if (isForward) {
            int startOffset = startStation.getPosition() - schedule.getStartStation().getPosition();
            int endOffset = endStation.getPosition() - schedule.getStartStation().getPosition();
            departTime = schedule.getDepartTime().plusMinutes(startOffset * 3);
            arrivalTime = schedule.getDepartTime().plusMinutes(endOffset * 3);
        } else {
            int startOffset = schedule.getStartStation().getPosition() - startStation.getPosition();
            int endOffset = schedule.getStartStation().getPosition() - endStation.getPosition();
            departTime = schedule.getDepartTime().plusMinutes(startOffset * 3);
            arrivalTime = schedule.getDepartTime().plusMinutes(endOffset * 3);
        }

        List<TicketType> ticketTypes = ticketTypeService.findByStartAndEnd(startStation.getStationId(), endStation.getStationId());
        Long ticketTypeId = ticketTypes.isEmpty() ? null : ticketTypes.get(0).getTicketTypeId();

        return new ScheduleResult(schedule, departTime, arrivalTime, travelMinutes, ticketTypeId);
    }


    private boolean isValidStationPair(TrainStation start, TrainStation end) {
        if (start == null || end == null) return false;
        String s = start.getName();
        String e = end.getName();
        return (s.equals("Bến Thành") && e.equals("Bến xe Suối Tiên")) ||
                (s.equals("Bến xe Suối Tiên") && e.equals("Bến Thành"));
    }
}