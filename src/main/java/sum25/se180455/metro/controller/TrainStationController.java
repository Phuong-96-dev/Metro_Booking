package sum25.se180455.metro.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import sum25.se180455.metro.pojo.TrainStation;
import sum25.se180455.metro.service.TrainStationService;

import java.util.Optional;

@Controller
@RequestMapping("/train-stations")
public class TrainStationController {
    @Autowired
    private TrainStationService trainStationService;

    @GetMapping
    public String getAllTrainStations(Model model) {
        model.addAttribute("trainStations", trainStationService.getAllTrainStations());
        return "train/train-station-list";
    }

    @GetMapping("/{id}")
    public String getTrainStationById(@PathVariable Long id, Model model) {
        Optional<TrainStation> station = trainStationService.getTrainStationById(id);
        model.addAttribute("station", station.orElse(null));
        return "train/train-station-detail";
    }
}