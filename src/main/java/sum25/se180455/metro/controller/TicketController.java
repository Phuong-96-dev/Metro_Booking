package sum25.se180455.metro.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import sum25.se180455.metro.pojo.*;
import sum25.se180455.metro.service.MemberService;
import sum25.se180455.metro.service.TicketService;
import sum25.se180455.metro.service.TicketTypeService;
import sum25.se180455.metro.service.TrainStationService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private TicketTypeService ticketTypeService;

    @Autowired
    private TrainStationService trainStationService;

    @GetMapping("/ticket")
    public String showAllTicketTypes(Model model) {
        List<TicketType> types = ticketTypeService.getAllTicketTypes();
        model.addAttribute("ticketTypes", types);

        // Bổ sung danh sách ga vào model
        List<TrainStation> stations = trainStationService.getAllTrainStations();
        model.addAttribute("stations", stations);
        model.addAttribute("isSearch", false);

        return "ticket";
    }

    @PostMapping("/ticket/buy")
    public String buyTicket(@RequestParam("ticketTypeId") Long ticketTypeId,
                            HttpSession session) {
        Member member = (Member) session.getAttribute("member");
        if (member == null) {
            return "redirect:/login";
        }

        Optional<TicketType> optionalTicketType = ticketTypeService.getTicketTypeById(ticketTypeId);
        if (optionalTicketType.isEmpty()) {
            return "redirect:/ticket";
        }

        TicketType ticketType = optionalTicketType.get();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dateEnd;

        if ("Vé lượt".equals(ticketType.getName())) {
            // Nếu là vé lượt → hết hạn sau 3 tiếng
            dateEnd = now.plusHours(3);
        } else {
            Integer durationDays = ticketType.getDurationDays();
            if (durationDays != null && durationDays > 0) {
                dateEnd = now.plusDays(durationDays);
            } else {
                dateEnd = null; // Hoặc bạn có thể thiết lập mặc định khác
            }
        }

        Ticket ticket = new Ticket();
        ticket.setStatus("Chưa thanh toán");
        ticket.setDate_start(now);
        ticket.setDate_end(dateEnd);
        ticket.setPrice(ticketType.getPrice());
        ticket.setMember(member);
        ticket.setTicketType(ticketType);

        Ticket savedTicket = ticketService.save(ticket);
        return "redirect:/payment?ticketId=" + savedTicket.getTicketId();
    }

    @GetMapping("/ticket/search")
    public String searchTickets(@RequestParam(required = false) Long start,
                                @RequestParam(required = false) Long end,
                                Model model) {
        List<TicketType> allTickets = ticketTypeService.getAllTicketTypes(); // Lấy tất cả vé
        List<TicketType> filteredTickets = new ArrayList<>();

        // Lọc vé lượt dựa trên start và end
        if (start != null && end != null) {
            filteredTickets = ticketTypeService.findByStartAndEnd(start, end);
        } else if (start != null) {
            filteredTickets = ticketTypeService.findByStart(start);
        } else if (end != null) {
            filteredTickets = ticketTypeService.findByEnd(end);
        } else {
            filteredTickets = allTickets; // Nếu không có tham số, lấy tất cả
        }

        // Thêm vé theo thời gian (stationStart và stationEnd là null)
        List<TicketType> timeBasedTickets = allTickets.stream()
                .filter(type -> type.getStationStart() == null && type.getStationEnd() == null)
                .toList();
        filteredTickets.addAll(timeBasedTickets);

        model.addAttribute("ticketTypes", filteredTickets);

        // Truyền danh sách các ga
        List<TrainStation> stations = trainStationService.getAllTrainStations();
        model.addAttribute("stations", stations);
        model.addAttribute("isSearch", true);

        return "ticket";
    }

    @PostMapping("/ticket/use")
    public String useTicket(@RequestParam Long ticketId, HttpSession session) {
        Ticket ticket = ticketService.getTicketById(ticketId);

        if (ticket != null && ticket.getTicketType() != null) {
            String ticketName = ticket.getTicketType().getName();
            String currentStatus = ticket.getStatus();

            // Chỉ xử lý nếu vé chưa bị "Hết hạn" hoặc "Đã dùng"
            if (!"Hết hạn".equals(currentStatus)) {
                if ("Vé lượt".equals(ticketName)) {
                    ticket.setStatus("Hết hạn");
                } else {
                    ticket.setStatus("Đang sử dụng");
                }
                ticketService.save(ticket);
            }
        }

        return "redirect:/feedback";
    }

}