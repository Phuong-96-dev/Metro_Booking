package sum25.se180455.metro.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sum25.se180455.metro.pojo.TrainStation;
import sum25.se180455.metro.repository.TrainStationRepository;

import java.util.List;
import java.util.Optional;

@Service
public class TrainStationServiceImpl implements TrainStationService {
    @Autowired
    private TrainStationRepository trainStationRepository;
    @Override
    public List<TrainStation> getAllTrainStations() {
        return trainStationRepository.findAll();
    }
    @Override
    public Optional<TrainStation> getTrainStationById(Long id) {
        return trainStationRepository.findById(id);
    }
    @Override
    public Optional<TrainStation> getTrainStationByName(String name) {
        return trainStationRepository.findByName(name);
    }

    @Override
    public TrainStation getStationById(Long id) {
        return trainStationRepository.getOne(id);
    }
}