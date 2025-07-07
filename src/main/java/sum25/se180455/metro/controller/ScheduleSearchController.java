package sum25.se180455.metro.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import sum25.se180455.metro.pojo.TrainSchedule;
import sum25.se180455.metro.pojo.TrainStation;
import sum25.se180455.metro.service.TrainScheduleService;
import sum25.se180455.metro.service.TrainStationService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/search-schedules")
public class ScheduleSearchController {
    @Autowired
    private TrainScheduleService trainScheduleService;

    @Autowired
    private TrainStationService trainStationService;

    @GetMapping
    public String showSearchForm(Model model) {
        model.addAttribute("stations", trainStationService.getAllTrainStations());
        return "train/schedule-search-form";
    }

    @PostMapping
    public String searchSchedules(@RequestParam Long startStationId, @RequestParam Long endStationId,
                                  @RequestParam LocalDateTime dateTime, Model model) {
        TrainStation startStation = trainStationService.getTrainStationById(startStationId).orElse(null);
        TrainStation endStation = trainStationService.getTrainStationById(endStationId).orElse(null);

        if (startStation == null || endStation == null || startStation.equals(endStation)) {
            model.addAttribute("error", "Ga bắt đầu hoặc kết thúc không hợp lệ.");
            model.addAttribute("stations", trainStationService.getAllTrainStations());
            return "train/schedule-search-form";
        }

        // Tìm lịch trình phù hợp
        List<TrainSchedule> schedules = trainScheduleService.getAllTrainSchedules().stream()
                .filter(schedule -> isValidSchedule(schedule, startStation, endStation, dateTime))
                .collect(Collectors.toList());

        // Tính thời gian di chuyển
        List<ScheduleResult> results = schedules.stream()
                .map(schedule -> {
                    int stationCount = Math.abs(startStation.getPosition() - endStation.getPosition());
                    long travelMinutes = stationCount * 2; // Giả định 2 phút mỗi ga
                    return new ScheduleResult(schedule, travelMinutes);
                })
                .collect(Collectors.toList());

        model.addAttribute("results", results);
        model.addAttribute("stations", trainStationService.getAllTrainStations());
        return "train/schedule-search-results";
    }

    private boolean isValidSchedule(TrainSchedule schedule, TrainStation startStation, TrainStation endStation, LocalDateTime dateTime) {
        // Kiểm tra lịch trình có chứa cả hai ga theo đúng thứ tự
        boolean isForward = schedule.getStartStation().getName().equals("Bến Thành");
        int startPos = startStation.getPosition();
        int endPos = endStation.getPosition();

        if (isForward) {
            return startPos <= endPos && startPos >= schedule.getStartStation().getPosition() &&
                    endPos <= schedule.getEndStation().getPosition() &&
                    schedule.getDepartTime().toLocalDate().equals(dateTime.toLocalDate());
        } else {
            return startPos >= endPos && startPos <= schedule.getStartStation().getPosition() &&
                    endPos >= schedule.getEndStation().getPosition() &&
                    schedule.getDepartTime().toLocalDate().equals(dateTime.toLocalDate());
        }
    }
    public static class ScheduleResult {
        private TrainSchedule schedule;
        private long travelMinutes;

        public ScheduleResult(TrainSchedule schedule, long travelMinutes) {
            this.schedule = schedule;
            this.travelMinutes = travelMinutes;
        }

        public TrainSchedule getSchedule() { return schedule; }
        public long getTravelMinutes() { return travelMinutes; }
    }
}

