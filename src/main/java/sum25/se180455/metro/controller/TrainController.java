package sum25.se180455.metro.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import sum25.se180455.metro.pojo.Train;
import sum25.se180455.metro.service.TrainService;

import java.util.Optional;

@Controller
@RequestMapping("/trains")
public class TrainController {
    @Autowired
    private TrainService trainService;

    @GetMapping
    public String getAllTrains(Model model) {
        model.addAttribute("trains", trainService.getAllTrains());
        return "train/train-list";
    }

    @GetMapping("/{id}")
    public String getTrainById(@PathVariable Long id, Model model) {
        Optional<Train> train = trainService.getTrainById(id);
        model.addAttribute("train", train.orElse(null));
        return "train/train-detail";
    }
}