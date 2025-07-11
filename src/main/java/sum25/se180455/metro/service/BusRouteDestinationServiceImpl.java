package sum25.se180455.metro.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sum25.se180455.metro.pojo.BusRouteDestination;
import sum25.se180455.metro.repository.BusRouteDestinationRepository;

import java.util.Optional;
@Service
public class BusRouteDestinationServiceImpl implements BusRouteDestinationService {
    @Autowired
    BusRouteDestinationRepository busRouteDestinationRepository;

    @Override
    public BusRouteDestination save(BusRouteDestination busRouteDestination) {
        return busRouteDestinationRepository.save(busRouteDestination);
    }

    @Override
    public Optional<BusRouteDestination> getBusRouteDestinationById(Long id) {
        return busRouteDestinationRepository.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        busRouteDestinationRepository.deleteById(id);
    }
}