package sum25.se180455.metro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sum25.se180455.metro.pojo.Ticket;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByMember_MemberId(Long memberId);

    int countByStatus(String status);

    List<Ticket> findByMember_MemberIdAndStatus(Long memberId, String status);
    @Query(value = "SELECT FORMAT(t.date_start, 'yyyy-MM-dd') AS day, SUM(t.price) " +
            "FROM ticket t " +
            "WHERE t.status = 'ACTIVE' " +
            "AND CAST(t.date_start AS DATE) BETWEEN :startDate AND :endDate " +
            "GROUP BY FORMAT(t.date_start, 'yyyy-MM-dd') " +
            "ORDER BY day",
            nativeQuery = true)
    List<Object[]> getRevenueGroupedByDayBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query(value = "SELECT MONTH(t.date_start) AS monthNum, SUM(t.price) FROM ticket t WHERE t.status = 'ACTIVE' AND YEAR(t.date_start) = :year GROUP BY MONTH(t.date_start) ORDER BY MONTH(t.date_start)", nativeQuery = true)
    List<Object[]> getRevenueGroupedByMonth(@Param("year") int year);

    @Query(value = "SELECT YEAR(t.date_start) AS year, SUM(t.price) " +
            "FROM ticket t " +
            "WHERE t.status = 'ACTIVE' " +
            "GROUP BY YEAR(t.date_start) " +
            "ORDER BY YEAR(t.date_start)",
            nativeQuery = true)
    List<Object[]> getRevenueGroupedByYear();

    @Query(value = "SELECT tt.name AS typeName, SUM(t.price) " +
            "FROM ticket t JOIN ticket_type tt ON t.ticket_type_id = tt.ticket_type_id " +
            "WHERE t.status = 'ACTIVE' GROUP BY tt.name", nativeQuery = true)
    List<Object[]> getRevenueByTicketType();

    @Query(value = "SELECT SUM(t.price) FROM ticket t WHERE t.status = 'ACTIVE'", nativeQuery = true)
    Double getTotalRevenue();
}