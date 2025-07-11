package sum25.se180455.metro.controller;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import sum25.se180455.metro.pojo.Member;
import sum25.se180455.metro.pojo.Ticket;
import sum25.se180455.metro.pojo.TicketType;
import sum25.se180455.metro.pojo.TrainSchedule;
import sum25.se180455.metro.service.MemberService;

import sum25.se180455.metro.service.TicketService;
import sum25.se180455.metro.service.TicketTypeService;
import sum25.se180455.metro.service.TrainScheduleService;

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
    private TrainScheduleService trainScheduleService;

    @GetMapping("/ticket")
    public String showAllTicketTypes(Model model) {
        List<TicketType> types = ticketTypeService.getAllTicketTypes();
        model.addAttribute("ticketTypes", types);

        return "ticket"; // lúc này ticket.html sẽ hiển thị ticketTypes
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
        ticket.setStatus("payment");
        ticket.setDate_start(now);
        ticket.setDate_end(dateEnd);
        ticket.setPrice(ticketType.getPrice());
        ticket.setMember(member);
        ticket.setTicketType(ticketType);

        Ticket savedTicket = ticketService.save(ticket);  // đảm bảo có ID
        return "redirect:/payment?ticketId=" + savedTicket.getTicketId();
    }



}


