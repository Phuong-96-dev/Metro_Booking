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
        model.addAttribute("stations", getTerminalStations());
        return "train/train-schedule-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Optional<TrainSchedule> scheduleOpt = trainScheduleService.getTrainScheduleById(id);
        if (scheduleOpt.isEmpty()) return "redirect:/train-schedules";

        model.addAttribute("schedule", scheduleOpt.get());
        model.addAttribute("trains", trainService.getAllTrains());
        model.addAttribute("stations", getTerminalStations());
        return "train/train-schedule-form";
    }

    @PostMapping
    public String saveSchedule(
            @RequestParam Long startStationId,
            @RequestParam Long endStationId,
            @ModelAttribute("schedule") TrainSchedule schedule,
            Model model) {

        Optional<TrainStation> startStation = trainStationService.getTrainStationById(startStationId);
        Optional<TrainStation> endStation = trainStationService.getTrainStationById(endStationId);

        if (startStation.isEmpty() || endStation.isEmpty()) {
            model.addAttribute("error", "Không tìm thấy ga bắt đầu hoặc ga kết thúc.");
            return reloadForm(model, schedule);
        }

        schedule.setStartStation(startStation.get());
        schedule.setEndStation(endStation.get());

        if (!isValidStationPair(schedule.getStartStation(), schedule.getEndStation())) {
            model.addAttribute("error", "Cặp ga không hợp lệ. Phải là Bến Thành đến Bến xe Suối Tiên hoặc ngược lại.");
            return reloadForm(model, schedule);
        }

        if (schedule.getDepartTime() == null) {
            model.addAttribute("error", "Vui lòng nhập giờ khởi hành.");
            return reloadForm(model, schedule);
        }

        // Tự động tính giờ đến: giờ khởi hành + 39 phút
        schedule.setArrivalTime(schedule.getDepartTime().plusMinutes(39));

        if (schedule.getTrain() == null || schedule.getTrain().getTrainId() == null) {
            model.addAttribute("error", "Vui lòng chọn tàu.");
            return reloadForm(model, schedule);
        }

        Optional<Train> trainOpt = trainService.getTrainById(schedule.getTrain().getTrainId());
        if (trainOpt.isEmpty()) {
            model.addAttribute("error", "Tàu không hợp lệ.");
            return reloadForm(model, schedule);
        }

        schedule.setTrain(trainOpt.get());
        trainScheduleService.saveTrainSchedule(schedule);

        return "redirect:/train-schedules";
    }

    @GetMapping("/delete/{id}")
    public String deleteSchedule(@PathVariable Long id) {
        trainScheduleService.deleteTrainSchedule(id);
        return "redirect:/train-schedules";
    }

    private boolean isValidStationPair(TrainStation start, TrainStation end) {
        if (start == null || end == null) return false;
        String s = start.getName();
        String e = end.getName();
        return (s.equals("Bến Thành") && e.equals("Bến xe Suối Tiên")) ||
                (s.equals("Bến xe Suối Tiên") && e.equals("Bến Thành"));
    }

    private List<TrainStation> getTerminalStations() {
        List<TrainStation> result = new ArrayList<>();
        trainStationService.getTrainStationByName("Bến Thành").ifPresent(result::add);
        trainStationService.getTrainStationByName("Bến xe Suối Tiên").ifPresent(result::add);
        return result;
    }

    private String reloadForm(Model model, TrainSchedule schedule) {
        model.addAttribute("schedule", schedule);
        model.addAttribute("trains", trainService.getAllTrains());
        model.addAttribute("stations", getTerminalStations());
        return "train/train-schedule-form";
    }
}