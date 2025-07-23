package sum25.se180455.metro.service;

import sum25.se180455.metro.pojo.BusRoute;

import java.util.List;
import java.util.Optional;

public interface BusRouteService {
    public List<BusRoute> getAllBusRoutes();
    public Optional<BusRoute> getBusRouteById(Long id);
    List<BusRoute> findBusRoutesByStation(Long stationId);
    int countByBusStatus(String busStatus);


}
