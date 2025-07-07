package sum25.se180455.metro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sum25.se180455.metro.pojo.TrainStation;

import java.util.Optional;

@Repository
public interface TrainStationRepository extends JpaRepository<TrainStation,Long> {
    Optional<TrainStation> findByName(String name);
}
