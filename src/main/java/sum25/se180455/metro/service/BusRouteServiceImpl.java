package sum25.se180455.metro.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sum25.se180455.metro.pojo.BusRoute;
import sum25.se180455.metro.pojo.TrainSchedule;
import sum25.se180455.metro.pojo.TrainStation;
import sum25.se180455.metro.repository.BusRouteRepository;
import sum25.se180455.metro.repository.TrainScheduleRepository;
import sum25.se180455.metro.repository.TrainStationRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BusRouteServiceImpl implements BusRouteService {
    @Autowired
    private BusRouteRepository busRouteRepository;
    @Autowired
    private TrainStationRepository trainStationRepository;

    @Override
    public List<BusRoute> getAllBusRoutes() {
        return busRouteRepository.findAll();
    }

    @Override
    public Optional<BusRoute> getBusRouteById(Long id) {
        return busRouteRepository.findById(id);
    }

    @Override
    public List<BusRoute> findBusRoutesByStation(Long stationId) {
        return busRouteRepository.findAll().stream()
                .filter(busRoute -> busRoute.getStation() != null
                        && busRoute.getStation().getStationId().equals(stationId))
                .collect(Collectors.toList());
    }
    @Override
    public int  countByBusStatus(String status) {
        return busRouteRepository.countByBusStatus(status);
    }
    }

