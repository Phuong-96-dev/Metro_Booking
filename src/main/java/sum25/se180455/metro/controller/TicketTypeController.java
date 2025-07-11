package sum25.se180455.metro.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import sum25.se180455.metro.pojo.TicketType;
import sum25.se180455.metro.pojo.TrainStation;
import sum25.se180455.metro.repository.TrainStationRepository;
import sum25.se180455.metro.service.TicketTypeService;
import sum25.se180455.metro.service.TrainStationService;

import java.util.List;

@Controller
@RequestMapping("/adminManage/manageTicketType")
public class TicketTypeController {

    @Autowired
    private TicketTypeService ticketTypeService;

    @Autowired
    private TrainStationRepository trainStationRepository;

    @Autowired
    private TrainStationService trainStationService;

    @GetMapping
    public String viewAllTicketTypes(Model model) {
        List<TicketType> ticketTypes = ticketTypeService.getAllTicketTypes();
        List<TrainStation> stations = trainStationService.getAllTrainStations(); // Cần thêm service lấy danh sách ga tàu
        model.addAttribute("ticketTypes", ticketTypes);
        model.addAttribute("stations", trainStationService.getAllTrainStations());

        return "adminManage/manageTicketType";
    }

    @PostMapping("/update/{id}")
    public String updateTicketType(
            @PathVariable("id") Long ticketTypeId,
            @RequestParam("name") String name,
            @RequestParam("price") Double price,
            @RequestParam(value = "durationDays", required = false) Integer durationDays,
            @RequestParam(value = "stationStartId", required = false) Long stationStartId,
            @RequestParam(value = "stationEndId", required = false) Long stationEndId
    ) {
        TrainStation stationStart = (stationStartId != null) ? trainStationService.getStationById(stationStartId) : null;
        TrainStation stationEnd = (stationEndId != null) ? trainStationService.getStationById(stationEndId) : null;

        TicketType newTicket = new TicketType();
        newTicket.setName(name);
        newTicket.setPrice(price);
        newTicket.setDurationDays(durationDays);
        newTicket.setStationStart(stationStart);
        newTicket.setStationEnd(stationEnd);

        ticketTypeService.updateTicketTypeById(ticketTypeId, newTicket);

        return "redirect:/adminManage/manageTicketType";
    }


    @PostMapping("/delete/{id}")
    public String deleteTicketType(@PathVariable("id") Long id) {
        ticketTypeService.deleteTicketTypeById(id);
        return "redirect:/adminManage/manageTicketType";

    }



    @PostMapping("/create")
    public String createTicketType(@RequestParam String name,
                                   @RequestParam Double price,
                                   @RequestParam(required = false) Integer durationDays,
                                   @RequestParam(required = false) Long stationStartId,
                                   @RequestParam(required = false) Long stationEndId) {

        TicketType ticket = new TicketType();
        ticket.setName(name);
        ticket.setPrice(price);

        if ("Vé lượt".equals(name)) {
            // Vé lượt không có thời hạn, có ga đi - ga đến
            ticket.setDurationDays(null);
            ticket.setStationStart(trainStationRepository.findById(stationStartId).orElse(null));
            ticket.setStationEnd(trainStationRepository.findById(stationEndId).orElse(null));
        } else {
            // Vé theo thời gian có thời hạn, không có ga đi/đến
            ticket.setDurationDays(durationDays);
            ticket.setStationStart(null);
            ticket.setStationEnd(null);
        }

        ticketTypeService.createTicketType(ticket);
        return "redirect:/adminManage/manageTicketType";
    }








}
