package sum25.se180455.metro.service;

import sum25.se180455.metro.pojo.Member;
import sum25.se180455.metro.pojo.Ticket;
import sum25.se180455.metro.pojo.TicketType;

import java.util.List;
import java.util.Optional;

public interface TicketService {
    public Ticket save(Ticket ticket);
    public Ticket findById(Long TicketId);
    List<Ticket> findByMember_MemberId(Long memberId);
    Optional<Ticket> findByTicketId(Long TicketId);
    Ticket getTicketById(Long TicketId);
    int countByStatus(String status);

    List<Ticket> getTicketsByMemberId(Long memberId);
    Double getTotalPaidByMemberId(Long memberId);
}
