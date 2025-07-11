package sum25.se180455.metro.service;

import sum25.se180455.metro.pojo.Ticket;
import sum25.se180455.metro.pojo.TicketType;

import java.util.List;
import java.util.Optional;

public interface TicketTypeService {
    List<TicketType> getAllTicketTypes();
    Optional<TicketType> getTicketTypeById(Long ticketTypeId);
    public TicketType updateTicketTypeById(Long ticketTypeId, TicketType updatedTicketType);
    public void deleteTicketTypeById(Long ticketTypeId);
    public  TicketType createTicketType(TicketType ticketType);

    List<TicketType> findByStartAndEnd(Long startStationId, Long endStationId);
    List<TicketType> findByStart(Long startStationId);
    List<TicketType> findByEnd(Long endStationId);


}
