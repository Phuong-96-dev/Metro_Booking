package sum25.se180455.metro.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sum25.se180455.metro.pojo.Member;
import sum25.se180455.metro.pojo.Ticket;
import sum25.se180455.metro.pojo.TicketType;
import sum25.se180455.metro.repository.TicketRepository;

import java.util.List;
import java.util.Optional;

@Service
public class TicketServiceImpl implements TicketService {
    @Autowired
    private TicketRepository ticketRepository;


    @Override
    public Ticket save(Ticket ticket) {
        return ticketRepository.save(ticket);
    }


    @Override
    public Ticket findById(Long ticketId) {
        return ticketRepository.findById(ticketId).orElse(null);
    }

    @Override
    public List<Ticket> findByMember_MemberId(Long memberId) {
        return ticketRepository.findByMember_MemberId(memberId);
    }


    @Override
    public Optional<Ticket> findByTicketId(Long TicketId) {
        return ticketRepository.findById(TicketId);
    }

    @Override
    public Ticket getTicketById(Long TicketId) {
        return ticketRepository.findById(TicketId).orElse(null);
    }

}
