package sum25.se180455.metro.controller;

import sum25.se180455.metro.service.ScanTicketService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PageController {

    private final ScanTicketService scanTicketService;

    public PageController(ScanTicketService scanTicketService) {
        this.scanTicketService = scanTicketService;
    }

    /** Trang cho nhân viên soát vé. Nên giới hạn ROLE_STAFF khi tích hợp Spring Security. */
    @GetMapping("/scanner")
    public String scanner() {
        return "adminManage/scanner";
    }

    /** Trang hiển thị vé của người dùng (QR + barcode). Cần kiểm tra chủ vé khi tích hợp login. */
    @GetMapping("/tickets/{code}")
    public String ticket(@PathVariable String code, Model model) {
        model.addAttribute("ticket", scanTicketService.getByCode(code));
        return "ticket";
    }
}
