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
import sum25.se180455.metro.request.ScheduleResult;
import sum25.se180455.metro.service.BusRouteService;
import sum25.se180455.metro.service.TrainScheduleService;
import sum25.se180455.metro.service.TrainStationService;

import java.time.LocalTime;
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

        // Calculate travel time
        int stationCount = Math.abs(startStation.getPosition() - endStation.getPosition());
        long travelMinutes = stationCount * 3; // 3 minutes per station

        // Find matching schedules
        List<ScheduleResult> results = trainScheduleService.getAllTrainSchedules().stream()
                .filter(schedule -> isValidSchedule(schedule, startStation, endStation))
                .map(schedule -> {
                    // Calculate specific departure and arrival times
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

                    return new ScheduleResult(schedule, departTime, arrivalTime, travelMinutes);
                })
                .collect(Collectors.toList());

        // Get bus routes at end station
        List<BusRoute> busRoutesAtEndStation = busRouteService.findBusRoutesByStation(endStationId);

        // Pass data to model
        model.addAttribute("results", results);
        model.addAttribute("stations", trainStationService.getAllTrainStations());
        model.addAttribute("busRoutes", busRoutesAtEndStation);
        model.addAttribute("startStation", startStation);
        model.addAttribute("endStation", endStation);
        model.addAttribute("startStationId", startStationId);
        model.addAttribute("endStationId", endStationId);
        model.addAttribute("travelMinutes", travelMinutes);

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
}