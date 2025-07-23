package sum25.se180455.metro.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sum25.se180455.metro.repository.TicketRepository;

import java.time.LocalDate;
import java.time.Month;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RenueService {
    @Autowired
    private TicketRepository ticketRepository;

    public double getTotalRevenue() {
        Double total = ticketRepository.getTotalRevenue();
        return total != null ? total : 0.0;
    }

    public Map<String, Double> getRevenueByTicketType() {
        List<Object[]> results = ticketRepository.getRevenueByTicketType();
        Map<String, Double> map = new LinkedHashMap<>();
        for (Object[] row : results) {
            map.put((String) row[0], (Double) row[1]);
        }
        return map;
    }

    public Map<String, Double> getDailyRevenue(LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = ticketRepository.getRevenueGroupedByDayBetween(startDate, endDate);
        Map<String, Double> dailyMap = new LinkedHashMap<>();
        for (Object[] row : results) {
            String day = (String) row[0];
            Double total = ((Number) row[1]).doubleValue();
            dailyMap.put(day, total);
        }
        return dailyMap;
    }

    public Map<String, Double> getMonthlyRevenue(int year) {
        List<Object[]> results = ticketRepository.getRevenueGroupedByMonth(year);
        Map<String, Double> monthlyMap = new LinkedHashMap<>();
        for (Month month : Month.values()) {
            monthlyMap.put("Tháng " + month.getValue(), 0.0);
        }
        for (Object[] row : results) {
            Integer monthNumber = ((Number) row[0]).intValue();
            Double total = (Double) row[1];
            monthlyMap.put("Tháng " + monthNumber, total);
        }
        return monthlyMap;
    }

    public Map<String, Double> getMonthlyRevenueCurrentYear() {
        return getMonthlyRevenue(LocalDate.now().getYear());
    }

    public Map<String, Double> getYearlyRevenue() {
        List<Object[]> results = ticketRepository.getRevenueGroupedByYear();
        Map<String, Double> yearlyMap = new LinkedHashMap<>();
        for (Object[] row : results) {
            Integer year = ((Number) row[0]).intValue();
            Double total = (Double) row[1];
            yearlyMap.put("Năm " + year, total);
        }
        return yearlyMap;
    }

}