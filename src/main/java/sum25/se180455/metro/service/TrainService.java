package sum25.se180455.metro.service;

import sum25.se180455.metro.pojo.Train;

import java.util.List;
import java.util.Optional;

public interface TrainService {
    public List<Train> getAllTrains();
    public Optional<Train> getTrainById(Long id);
}
