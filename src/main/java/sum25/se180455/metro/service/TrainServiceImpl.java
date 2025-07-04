package sum25.se180455.metro.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sum25.se180455.metro.pojo.Train;
import sum25.se180455.metro.repository.TrainRepository;

import java.util.List;
import java.util.Optional;

@Service
public class TrainServiceImpl implements TrainService {
    @Autowired
    private TrainRepository trainRepository;
    @Override
    public List<Train> getAllTrains() {
        return trainRepository.findAll();
    }
    @Override
    public Optional<Train> getTrainById(Long id) {
        return trainRepository.findById(id);
    }
}