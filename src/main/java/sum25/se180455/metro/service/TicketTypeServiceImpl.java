package sum25.se180455.metro.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sum25.se180455.metro.pojo.TicketType;
import sum25.se180455.metro.repository.TicketRepository;
import sum25.se180455.metro.repository.TicketTypeRepository;

import java.util.List;
@Service
public class TicketTypeServiceImpl implements TicketTypeService {
    @Autowired
    private TicketTypeRepository ticketTypeRepository;

    @Override
    public List<TicketType> getTicketTypes() {
        return ticketTypeRepository.findAll();
    }

    @Override
    public TicketType getTicketTypeById(Long id) {
        return ticketTypeRepository.getTicketTypeByTicketTypeId(id);
    }


}
