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

    @Override
    public List<Ticket> getUnpaidTickets(Long memberId) {
        return ticketRepository.findByMember_MemberIdAndStatus(memberId, "payment");
    }

    @Override
    public Ticket findById(Long TicketId) {
        return ticketRepository.findById(TicketId).get();
    }

    @Override
    public List<Ticket> getActiveTicketsByMemberId(Long memberId) {
        return ticketRepository.findByMember_MemberIdAndStatus(memberId, "Active");
    }


}
