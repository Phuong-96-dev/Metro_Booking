package sum25.se180455.metro.repository;

import sum25.se180455.metro.pojo.ScanTicket;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ScanTicketRepository extends JpaRepository<ScanTicket, Long> {

    Optional<ScanTicket> findByCode(String code);

    Optional<ScanTicket> findByOrderRef(String orderRef);

    boolean existsByCode(String code);

    List<ScanTicket> findByUserIdOrderByIssuedAtDesc(Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE ScanTicket t
               SET t.status = com.team.ticket.pojo.TicketStatus.USED,
                   t.usedAt = :now,
                   t.usedBy = :scannedBy
             WHERE t.code = :code
               AND t.status = com.team.ticket.pojo.TicketStatus.ACTIVE
               AND (t.expiresAt IS NULL OR t.expiresAt > :now)
            """)
    int markUsed(@Param("code") String code,
                 @Param("now") LocalDateTime now,
                 @Param("scannedBy") String scannedBy);
}
