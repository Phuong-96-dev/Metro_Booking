package sum25.se180455.metro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sum25.se180455.metro.pojo.TicketType;
@Repository

public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {


    TicketType getTicketTypeByTicketTypeId(Long ticketTypeId);

}
