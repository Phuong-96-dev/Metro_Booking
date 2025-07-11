package sum25.se180455.metro.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import sum25.se180455.metro.pojo.BusRoute;
import sum25.se180455.metro.pojo.BusRouteDestination;
import sum25.se180455.metro.service.BusRouteService;
import sum25.se180455.metro.service.BusRouteDestinationService;
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

    @Autowired
    private BusRouteDestinationService busRouteDestinationService;

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

    @PostMapping("/{busId}/destinations/create")
    public String createDestination(@PathVariable Long busId,
                                    @RequestParam String destination,
                                    @RequestParam Integer estimatedMinutes) {
        Optional<BusRoute> busRoute = busRouteService.getBusRouteById(busId);
        if (busRoute.isPresent()) {
            BusRouteDestination destinationObj = new BusRouteDestination();
            destinationObj.setBusRoute(busRoute.get());
            destinationObj.setDestination(destination);
            destinationObj.setEstimatedMinutes(estimatedMinutes);
            busRouteDestinationService.save(destinationObj);
        }
        return "redirect:/bus-routes/" + busId;
    }

    @PostMapping("/{busId}/destinations/edit/{id}")
    public String editDestination(@PathVariable Long busId,
                                  @PathVariable Long id,
                                  @RequestParam String destination,
                                  @RequestParam Integer estimatedMinutes) {
        Optional<BusRouteDestination> existingDestination = busRouteDestinationService.getBusRouteDestinationById(id);
        if (existingDestination.isPresent()) {
            BusRouteDestination destinationObj = existingDestination.get();
            destinationObj.setDestination(destination);
            destinationObj.setEstimatedMinutes(estimatedMinutes);
            busRouteDestinationService.save(destinationObj);
        }
        return "redirect:/bus-routes/" + busId;
    }

    @PostMapping("/{busId}/destinations/delete/{id}")
    public String deleteDestination(@PathVariable Long busId, @PathVariable Long id) {
        busRouteDestinationService.deleteById(id);
        return "redirect:/bus-routes/" + busId;
    }
}