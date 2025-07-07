package sum25.se180455.metro.service;

import sum25.se180455.metro.pojo.Ticket;
import sum25.se180455.metro.pojo.TicketType;

import java.util.List;

public interface TicketService {
    public void save(Ticket ticket);
    List<Ticket> getUnpaidTickets(Long memberId);
    public Ticket findById(Long TicketId);
    List<Ticket> getActiveTicketsByMemberId(Long memberId);
}
