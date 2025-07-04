package sum25.se180455.metro.service;

import sum25.se180455.metro.pojo.TicketType;

import java.util.List;

public interface TicketTypeService {
    public List<TicketType> getTicketTypes();
    public TicketType getTicketTypeById(Long id);

}
