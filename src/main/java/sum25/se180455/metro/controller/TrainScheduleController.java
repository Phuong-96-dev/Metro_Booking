package sum25.se180455.metro.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import sum25.se180455.metro.pojo.Train;
import sum25.se180455.metro.pojo.TrainSchedule;
import sum25.se180455.metro.pojo.TrainStation;
import sum25.se180455.metro.service.TrainScheduleService;
import sum25.se180455.metro.service.TrainService;
import sum25.se180455.metro.service.TrainStationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/train-schedules")
public class TrainScheduleController {
    @Autowired
    private TrainScheduleService trainScheduleService;

    @Autowired
    private TrainService trainService;

    @Autowired
    private TrainStationService trainStationService;

    @GetMapping
    public String getAllTrainSchedules(Model model) {
        model.addAttribute("schedules", trainScheduleService.getAllTrainSchedules());
        return "train/train-schedule-list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("schedule", new TrainSchedule());
        model.addAttribute("trains", trainService.getAllTrains());
        model.addAttribute("trainStations", trainStationService.getAllTrainStations());
        return "train/train-schedule-form";
    }

    @PostMapping
    public String addTrainSchedule(@ModelAttribute TrainSchedule schedule, Model model) {
        // Tìm Train từ trainId
        Optional<Train> train = trainService.getTrainById(schedule.getTrain().getTrainId());
        if (!train.isPresent()) {
            model.addAttribute("error", "Invalid train selected");
            model.addAttribute("trains", trainService.getAllTrains());
            model.addAttribute("trainStations", trainStationService.getAllTrainStations());
            return "train/train-schedule-form";
        }
        schedule.setTrain(train.get());

        // Tìm TrainStation tương ứng với stationStart và stationEnd để thêm vào stations
        List<TrainStation> stations = new ArrayList<>();
        for (TrainStation station : trainStationService.getAllTrainStations()) {
            if (station.getName().equals(schedule.getStationStart()) ||
                    station.getName().equals(schedule.getStationEnd())) {
                stations.add(station);
            }
        }
        schedule.setStations(stations);

        // Lưu TrainSchedule
        trainScheduleService.saveTrainSchedule(schedule);
        return "redirect:/train-schedules";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Optional<TrainSchedule> schedule = trainScheduleService.getTrainScheduleById(id);
        model.addAttribute("schedule", schedule.orElse(new TrainSchedule()));
        model.addAttribute("trains", trainService.getAllTrains());
        model.addAttribute("trainStations", trainStationService.getAllTrainStations());
        return "train/train-schedule-form";
    }

    @PostMapping("/update/{id}")
    public String updateTrainSchedule(@PathVariable Long id, @ModelAttribute TrainSchedule schedule, Model model) {
        // Tìm Train từ trainId
        Optional<Train> train = trainService.getTrainById(schedule.getTrain().getTrainId());
        if (!train.isPresent()) {
            model.addAttribute("error", "Invalid train selected");
            model.addAttribute("trains", trainService.getAllTrains());
            model.addAttribute("trainStations", trainStationService.getAllTrainStations());
            return "train/train-schedule-form";
        }
        schedule.setTrain(train.get());

        // Tìm TrainStation tương ứng với stationStart và stationEnd để thêm vào stations
        List<TrainStation> stations = new ArrayList<>();
        for (TrainStation station : trainStationService.getAllTrainStations()) {
            if (station.getName().equals(schedule.getStationStart()) ||
                    station.getName().equals(schedule.getStationEnd())) {
                stations.add(station);
            }
        }
        schedule.setStations(stations);

        // Cập nhật scheduleId và lưu
        schedule.setScheduleId(id);
        trainScheduleService.saveTrainSchedule(schedule);
        return "redirect:/train-schedules";
    }

    @GetMapping("/delete/{id}")
    public String deleteTrainSchedule(@PathVariable Long id) {
        trainScheduleService.deleteTrainSchedule(id);
        return "redirect:/train-schedules";
    }
}