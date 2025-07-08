package sum25.se180455.metro.controller;

import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import sum25.se180455.metro.pojo.BusRoute;
import sum25.se180455.metro.pojo.Train;
import sum25.se180455.metro.service.BusRouteService;
import sum25.se180455.metro.service.TrainStationService;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/bus-routes")
public class BusRouteController {

    @Autowired
    private BusRouteService busRouteService;

    @Autowired
    private TrainStationService trainStationService;

    @GetMapping("/search")
    public String showSearchForm(Model model) {
        model.addAttribute("stations", trainStationService.getAllTrainStations());
        return "busroute/search";
    }

    @PostMapping("/search")
    public String searchBusRoutes(@RequestParam Long stationId, Model model) {
        List<BusRoute> results = busRouteService.findBusRoutesByStation(stationId);
        model.addAttribute("results", results);
        model.addAttribute("stations", trainStationService.getAllTrainStations());
        return "busroute/search";
    }

    @GetMapping
    public String getAllBusRoutes(Model model) {
        model.addAttribute("buses", busRouteService.getAllBusRoutes());
        return "train/busroute-list";
    }

    @GetMapping("/{id}")
    public String getBusById(@PathVariable Long id, Model model) {
        Optional<BusRoute> bus = busRouteService.getBusRouteById(id);
        model.addAttribute("bus", bus.orElse(null));
        return "train/busroute-detail";
    }
}

