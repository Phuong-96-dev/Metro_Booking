package sum25.se180455.metro.service;

import sum25.se180455.metro.pojo.BusRouteDestination;

import java.util.Optional;

public interface BusRouteDestinationService {
    public BusRouteDestination save(BusRouteDestination busRouteDestination);
    public Optional<BusRouteDestination> getBusRouteDestinationById(Long id);
    public void deleteById(Long id);
}
