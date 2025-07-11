package sum25.se180455.metro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sum25.se180455.metro.pojo.Ticket;
import sum25.se180455.metro.pojo.TicketType;

import java.util.List;

@Repository

public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {


    TicketType getTicketTypeByTicketTypeId(Long ticketTypeId);
    List<TicketType> findByStationStart_StationIdAndStationEnd_StationId(Long startStationId, Long endStationId);

    List<TicketType> findByStationStart_StationId(Long startStationId);

    List<TicketType> findByStationEnd_StationId(Long endStationId);
}
