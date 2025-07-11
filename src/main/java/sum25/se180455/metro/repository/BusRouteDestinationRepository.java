package sum25.se180455.metro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sum25.se180455.metro.pojo.BusRouteDestination;

public interface BusRouteDestinationRepository extends JpaRepository<BusRouteDestination, Long> {
}