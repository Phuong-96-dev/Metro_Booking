package sum25.se180455.metro.controller;

import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import sum25.se180455.metro.pojo.BusRoute;
import sum25.se180455.metro.service.BusRouteService;
import sum25.se180455.metro.service.TrainStationService;

import java.util.List;

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
}

