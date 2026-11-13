package sum25.se180455.metro.controller;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;
import sum25.se180455.metro.pojo.Member;
import sum25.se180455.metro.pojo.Ticket;
import sum25.se180455.metro.pojo.TicketType;
import sum25.se180455.metro.pojo.TrainStation;
import sum25.se180455.metro.service.StudentVerificationService;
import sum25.se180455.metro.service.TicketService;
import sum25.se180455.metro.service.TicketTypeService;
import sum25.se180455.metro.service.TrainStationService;

import java.util.Optional;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketControllerTest {
    @Mock
    private TicketService ticketService;
    @Mock
    private TicketTypeService ticketTypeService;
    @Mock
    private StudentVerificationService studentVerificationService;
    @Mock
    private TrainStationService trainStationService;
    @InjectMocks
    private TicketController controller;

    @Test
    void unauthenticatedUserIsRedirectedToLogin() {
        assertEquals("redirect:/login", controller.buyTicket(1L, new MockHttpSession()));
        verifyNoInteractions(ticketService, ticketTypeService);
    }

    @Test
    void unverifiedMemberCannotCreateStudentTicket() {
        Member member = member();
        HttpSession session = session(member);
        TicketType type = new TicketType();
        type.setStudentTicket(true);
        when(ticketTypeService.getTicketTypeById(3L)).thenReturn(Optional.of(type));
        when(studentVerificationService.hasCurrentApproval(7L)).thenReturn(false);

        assertEquals("redirect:/student-verification?required", controller.buyTicket(3L, session));
        verify(ticketService, never()).save(any(Ticket.class));
    }

    @Test
    void ordinaryTicketPurchaseStillUsesTheExistingCreationFlow() {
        TicketType type = new TicketType();
        type.setName("Vé tháng");
        type.setDurationDays(30);
        type.setPrice(150000.0);
        when(ticketTypeService.getTicketTypeById(4L)).thenReturn(Optional.of(type));
        when(ticketService.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket ticket = invocation.getArgument(0);
            ticket.setTicketId(42L);
            return ticket;
        });

        assertEquals("redirect:/payment?ticketId=42",
                controller.buyTicket(4L, session(member())));
        verify(ticketService).save(argThat(ticket ->
                ticket != null
                        && ticket.getMember() != null
                        && Long.valueOf(7L).equals(ticket.getMember().getMemberId())
                        && ticket.getTicketType() == type
                        && "Chưa thanh toán".equals(ticket.getStatus())));
        verifyNoInteractions(studentVerificationService);
    }

    @Test
    void approvedMemberCanBuyStudentTicket() {
        TicketType type = new TicketType();
        type.setName("Vé sinh viên");
        type.setStudentTicket(true);
        type.setDurationDays(30);
        type.setPrice(100000.0);
        when(ticketTypeService.getTicketTypeById(5L)).thenReturn(Optional.of(type));
        when(studentVerificationService.hasCurrentApproval(7L)).thenReturn(true);
        when(ticketService.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket ticket = invocation.getArgument(0);
            ticket.setTicketId(43L);
            return ticket;
        });

        assertEquals("redirect:/payment?ticketId=43",
                controller.buyTicket(5L, session(member())));
        verify(ticketService).save(any(Ticket.class));
        verify(studentVerificationService).hasCurrentApproval(7L);
    }

    @Test
    void searchWithoutStationFiltersDoesNotDuplicateTimeTickets() {
        TicketType timeTicket = new TicketType();
        timeTicket.setName("Vé tháng");
        TicketType stationTicket = new TicketType();
        stationTicket.setName("Vé lượt");
        stationTicket.setStationStart(new TrainStation());

        when(ticketTypeService.getAllTicketTypes()).thenReturn(List.of(timeTicket, stationTicket));
        when(trainStationService.getAllTrainStations()).thenReturn(List.of());

        ExtendedModelMap model = new ExtendedModelMap();
        controller.searchTickets(null, null, model, new MockHttpSession());

        @SuppressWarnings("unchecked")
        List<TicketType> displayed = (List<TicketType>) Objects.requireNonNull(
                model.getAttribute("ticketTypes"), "ticketTypes should be present in the model");
        assertEquals(2, displayed.size());
        assertEquals(1, displayed.stream().filter(type -> type == timeTicket).count());
    }

    private Member member() {
        Member member = new Member();
        member.setMemberId(7L);
        return member;
    }

    private HttpSession session(Member member) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("member", member);
        return session;
    }
}
