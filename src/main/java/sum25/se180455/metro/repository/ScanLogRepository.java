package sum25.se180455.metro.repository;

import sum25.se180455.metro.pojo.ScanLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScanLogRepository extends JpaRepository<ScanLog, Long> {
    List<ScanLog> findTop50ByOrderByScannedAtDesc();
}
