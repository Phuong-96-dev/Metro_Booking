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

        // ✅ Bổ sung dòng này để truyền danh sách ga vào model
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
        LocalDateTime dateEnd = null;

        Integer durationDays = ticketType.getDurationDays();
        if (durationDays != null && durationDays > 0) {
            dateEnd = now.plusDays(durationDays);
        }

        Ticket ticket = new Ticket();
        ticket.setStatus("Chưa thanh toán");
        ticket.setDate_start(now);
        ticket.setDate_end(dateEnd);
        ticket.setPrice(ticketType.getPrice());
        ticket.setMember(member);
        ticket.setTicketType(ticketType);

        Ticket savedTicket = ticketService.save(ticket);  // đảm bảo có ID
        return "redirect:/payment?ticketId=" + savedTicket.getTicketId();
    }

    @GetMapping("/ticket/search")
    public String searchTickets(@RequestParam(required = false) Long start,
                                @RequestParam(required = false) Long end,
                                Model model) {
        List<TicketType> tickets;

        if (start != null && end != null) {
            tickets = ticketTypeService.findByStartAndEnd(start, end);
        } else if (start != null) {
            tickets = ticketTypeService.findByStart(start);
        } else if (end != null) {
            tickets = ticketTypeService.findByEnd(end);
        } else {
            tickets = ticketTypeService.getAllTicketTypes();
        }

        model.addAttribute("ticketTypes", tickets);

        // ✅ Truyền danh sách các ga
        List<TrainStation> stations = trainStationService.getAllTrainStations();
        model.addAttribute("stations", stations);
        model.addAttribute("isSearch", true);

        // ❌ KHÔNG dùng redirect
        return "ticket"; // <== thay đổi đường dẫn HTML đúng
    }

    @PostMapping("/ticket/use")
    public String useTicket(@RequestParam Long ticketId, HttpSession session) {
        Ticket ticket = ticketService.getTicketById(ticketId);

        if (ticket != null
                && ticket.getTicketType() != null
                && "Vé lượt".equals(ticket.getTicketType().getName())
                && !"Đã dùng".equals(ticket.getStatus())) {

            ticket.setStatus("Đã dùng");
            ticketService.save(ticket); // chỉ cập nhật nếu là "Vé lượt"
        }

        return "redirect:/feedback";
    }




}


