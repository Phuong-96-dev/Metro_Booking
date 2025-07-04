package sum25.se180455.metro.service;

import sum25.se180455.metro.pojo.TrainStation;

import java.util.List;
import java.util.Optional;

public interface TrainStationService {
    public List<TrainStation> getAllTrainStations();
    public Optional<TrainStation> getTrainStationById(Long id);
}
