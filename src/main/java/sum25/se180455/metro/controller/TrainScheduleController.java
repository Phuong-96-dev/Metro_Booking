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

import java.time.LocalTime;
import java.util.*;

@Controller
@RequestMapping("/train-schedules")
public class TrainScheduleController {

    @Autowired
    private TrainScheduleService trainScheduleService;

    @Autowired
    private TrainService trainService;

    @GetMapping
    public String getAllTrainSchedules(Model model) {
        model.addAttribute("schedules", trainScheduleService.getAllTrainSchedules());
        return "train/train-schedule-list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("schedule", new TrainSchedule());
        model.addAttribute("trains", trainService.getAllTrains());
        model.addAttribute("stations", trainScheduleService.getTerminalStations());
        return "train/train-schedule-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Optional<TrainSchedule> scheduleOpt = trainScheduleService.getTrainScheduleById(id);
        if (scheduleOpt.isEmpty()) return "redirect:/train-schedules";

        model.addAttribute("schedule", scheduleOpt.get());
        model.addAttribute("trains", trainService.getAllTrains());
        model.addAttribute("stations", trainScheduleService.getTerminalStations());
        return "train/train-schedule-form";
    }

    @PostMapping
    public String saveSchedule(
            @RequestParam Long startStationId,
            @RequestParam Long endStationId,
            @ModelAttribute("schedule") TrainSchedule schedule,
            Model model) {
        try {
            trainScheduleService.validateAndSaveSchedule(schedule, startStationId, endStationId);
            return "redirect:/train-schedules";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("schedule", schedule);
            model.addAttribute("trains", trainService.getAllTrains());
            model.addAttribute("stations", trainScheduleService.getTerminalStations());
            return "train/train-schedule-form";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteSchedule(@PathVariable Long id) {
        trainScheduleService.deleteTrainSchedule(id);
        return "redirect:/train-schedules";
    }
}
