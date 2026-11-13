package sum25.se180455.metro.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import sum25.se180455.metro.pojo.Ticket;
import sum25.se180455.metro.pojo.TicketType;
import sum25.se180455.metro.repository.TicketTypeRepository;

import java.util.List;
import java.util.Optional;

@Service
public class TicketTypeServiceImpl implements TicketTypeService {

    @Autowired
    private TicketTypeRepository ticketTypeRepository;

    @Override
    public List<TicketType> getAllTicketTypes() {
        return ticketTypeRepository.findAll();
    }

    @Override
    public Optional<TicketType> getTicketTypeById(Long ticketTypeId) {
        return Optional.ofNullable(ticketTypeRepository.getTicketTypeByTicketTypeId(ticketTypeId));
    }

    @Override
    public TicketType updateTicketTypeById(Long ticketTypeId, TicketType newTicket) {
        TicketType existing = ticketTypeRepository.findById(ticketTypeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy loại vé"));

        existing.setName(newTicket.getName());
        existing.setPrice(newTicket.getPrice());
        existing.setDurationDays(newTicket.getDurationDays()); // có thể null
        existing.setStudentTicket(newTicket.isStudentTicket());

        existing.setStationStart(newTicket.getStationStart()); // có thể null
        existing.setStationEnd(newTicket.getStationEnd());     // có thể null

        return ticketTypeRepository.save(existing);
    }

    @Override
    public void deleteTicketTypeById(Long ticketTypeId) {
        try {
            ticketTypeRepository.deleteById(ticketTypeId);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Không thể xóa loại vé đang được sử dụng trong vé.");
        }
    }

    @Override
    public TicketType createTicketType(TicketType ticketType) {
       return ticketTypeRepository.save(ticketType);
    }


    @Override
    public List<TicketType> findByStartAndEnd(Long startStationId, Long endStationId) {
        return ticketTypeRepository.findByStationStart_StationIdAndStationEnd_StationId(startStationId, endStationId);
    }

    @Override
    public List<TicketType> findByStart(Long startStationId) {
        return ticketTypeRepository.findByStationStart_StationId(startStationId);
    }

    @Override
    public List<TicketType> findByEnd(Long endStationId) {
        return ticketTypeRepository.findByStationEnd_StationId(endStationId);
    }


}
