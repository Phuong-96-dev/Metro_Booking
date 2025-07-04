package sum25.se180455.metro.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sum25.se180455.metro.pojo.Ticket;
import sum25.se180455.metro.pojo.TicketType;
import sum25.se180455.metro.repository.TicketRepository;

import java.util.List;
@Service
public class TicketServiceImpl implements TicketService {
    @Autowired
    private TicketRepository ticketRepository;


    @Override
    public void save(Ticket ticket) {
        ticketRepository.save(ticket);
    }
}
