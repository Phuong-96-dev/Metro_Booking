package sum25.se180455.metro.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import sum25.se180455.metro.pojo.BusRoute;
import sum25.se180455.metro.pojo.TicketType;
import sum25.se180455.metro.pojo.TrainSchedule;
import sum25.se180455.metro.pojo.TrainStation;
import sum25.se180455.metro.request.ScheduleResult;
import sum25.se180455.metro.service.BusRouteService;
import sum25.se180455.metro.service.TicketTypeService;
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

    @Autowired
    private TicketTypeService ticketTypeService;

    @GetMapping
    public String showSearchForm(Model model) {
        model.addAttribute("stations", trainStationService.getAllTrainStations());
        return "train/schedule-search";
    }

    @PostMapping
    public String searchSchedules(@RequestParam Long startStationId,
                                  @RequestParam Long endStationId,
                                  Model model) {

        if (startStationId.equals(endStationId)) {
            model.addAttribute("error", "Ga bắt đầu hoặc kết thúc không hợp lệ.");
            model.addAttribute("stations", trainStationService.getAllTrainStations());
            return "train/schedule-search";
        }

        List<ScheduleResult> results = trainScheduleService.searchSchedules(startStationId, endStationId);
        List<BusRoute> busRoutes = busRouteService.findBusRoutesByStation(endStationId);
        TrainStation startStation = trainStationService.getTrainStationById(startStationId).orElse(null);
        TrainStation endStation = trainStationService.getTrainStationById(endStationId).orElse(null);

        int travelMinutes = Math.abs(startStation.getPosition() - endStation.getPosition()) * 3;

        model.addAttribute("results", results);
        model.addAttribute("stations", trainStationService.getAllTrainStations());
        model.addAttribute("busRoutes", busRoutes);
        model.addAttribute("startStation", startStation);
        model.addAttribute("endStation", endStation);
        model.addAttribute("startStationId", startStationId);
        model.addAttribute("endStationId", endStationId);
        model.addAttribute("travelMinutes", travelMinutes);

        return "train/schedule-search";
    }
}