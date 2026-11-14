package sum25.se180455.metro.controller;

import sum25.se180455.metro.service.ScanTicketService;
import sum25.se180455.metro.pojo.ScanTicket;
import sum25.se180455.metro.security.MemberPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class PageController {

    private final ScanTicketService scanTicketService;

    public PageController(ScanTicketService scanTicketService) {
        this.scanTicketService = scanTicketService;
    }

    @GetMapping("/scanner")
    public String scanner(Model model, Authentication authentication) {
        model.addAttribute("scannerName", authentication.getName());
        return "adminManage/scanner";
    }

    @GetMapping("/tickets/{code}")
    public String ticket(@PathVariable String code, Model model, Authentication authentication) {
        ScanTicket ticket = scanTicketService.getByCode(code);
        requireOwnerOrAdmin(ticket, authentication);
        model.addAttribute("ticket", ticket);
        return "member/scanTicket";
    }

    private void requireOwnerOrAdmin(ScanTicket ticket, Authentication authentication) {
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        boolean owner = authentication.getPrincipal() instanceof MemberPrincipal principal
                && ticket.getUserId().equals(principal.getMemberId());
        if (!admin && !owner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }
}
