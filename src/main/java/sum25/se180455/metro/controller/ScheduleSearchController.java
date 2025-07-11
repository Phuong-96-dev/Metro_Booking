package sum25.se180455.metro.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import sum25.se180455.metro.pojo.BusRoute;
import sum25.se180455.metro.pojo.TrainSchedule;
import sum25.se180455.metro.pojo.TrainStation;
import sum25.se180455.metro.service.BusRouteService;
import sum25.se180455.metro.service.TrainScheduleService;
import sum25.se180455.metro.service.TrainStationService;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/search-schedules")
public class ScheduleSearchController {

    @Autowired
    private TrainScheduleService trainScheduleService;

    @Autowired
    private TrainStationService trainStationService;

    @Autowired
    private BusRouteService busRouteService;

    @GetMapping
    public String showSearchForm(Model model) {
        model.addAttribute("stations", trainStationService.getAllTrainStations());
        return "train/schedule-search";
    }

    @PostMapping
    public String searchSchedules(@RequestParam Long startStationId,
                                  @RequestParam Long endStationId,
                                  Model model) {

        TrainStation startStation = trainStationService.getTrainStationById(startStationId).orElse(null);
        TrainStation endStation = trainStationService.getTrainStationById(endStationId).orElse(null);

        if (startStation == null || endStation == null || startStation.equals(endStation)) {
            model.addAttribute("error", "Ga bắt đầu hoặc kết thúc không hợp lệ.");
            model.addAttribute("stations", trainStationService.getAllTrainStations());
            return "train/schedule-search";
        }

        // Find matching schedules
        List<TrainSchedule> schedules = trainScheduleService.getAllTrainSchedules().stream()
                .filter(schedule -> isValidSchedule(schedule, startStation, endStation))
                .collect(Collectors.toList());

        // Calculate travel time
        List<ScheduleResult> results = schedules.stream()
                .map(schedule -> {
                    int stationCount = Math.abs(startStation.getPosition() - endStation.getPosition());
                    long travelMinutes = stationCount * 2; // Assume 2 minutes per station
                    return new ScheduleResult(schedule, travelMinutes);
                })
                .collect(Collectors.toList());

        // Get bus routes at end station
        List<BusRoute> busRoutesAtEndStation = busRouteService.findBusRoutesByStation(endStationId);

        // Pass data to model
        model.addAttribute("results", results);
        model.addAttribute("stations", trainStationService.getAllTrainStations());
        model.addAttribute("busRoutes", busRoutesAtEndStation);
        model.addAttribute("endStation", endStation);

        return "train/schedule-search";
    }

    // Check if schedule is valid
    private boolean isValidSchedule(TrainSchedule schedule, TrainStation startStation, TrainStation endStation) {
        boolean isForward = schedule.getStartStation().getName().equals("Bến Thành");
        int startPos = startStation.getPosition();
        int endPos = endStation.getPosition();

        if (isForward) {
            return startPos <= endPos && startPos >= schedule.getStartStation().getPosition() &&
                    endPos <= schedule.getEndStation().getPosition();
        } else {
            return startPos >= endPos && startPos <= schedule.getStartStation().getPosition() &&
                    endPos >= schedule.getEndStation().getPosition();
        }
    }

    // Class for schedule result + travel time
    public static class ScheduleResult {
        private TrainSchedule schedule;
        private long travelMinutes;

        public ScheduleResult(TrainSchedule schedule, long travelMinutes) {
            this.schedule = schedule;
            this.travelMinutes = travelMinutes;
        }

        public TrainSchedule getSchedule() {
            return schedule;
        }

        public long getTravelMinutes() {
            return travelMinutes;
        }
    }
}