package sum25.se180455.metro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sum25.se180455.metro.pojo.BusRoute;

import java.util.List;

@Repository
public interface BusRouteRepository extends JpaRepository<BusRoute,Long> {
    List<BusRoute> findByStation_StationId(Long stationId);
}
