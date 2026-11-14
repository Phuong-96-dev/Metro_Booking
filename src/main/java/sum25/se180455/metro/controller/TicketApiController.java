package sum25.se180455.metro.controller;

import sum25.se180455.metro.request.*;
import sum25.se180455.metro.pojo.ScanLog;
import sum25.se180455.metro.pojo.ScanResult;
import sum25.se180455.metro.pojo.ScanTicket;
import sum25.se180455.metro.security.MemberPrincipal;
import sum25.se180455.metro.service.QrCodeService;
import sum25.se180455.metro.service.ScanTicketService;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class TicketApiController {

    private final ScanTicketService scanTicketService;
    private final QrCodeService qrCodeService;

    public TicketApiController(ScanTicketService scanTicketService, QrCodeService qrCodeService) {
        this.scanTicketService = scanTicketService;
        this.qrCodeService = qrCodeService;
    }

    @PostMapping("/tickets")
    public ScanTicketResponse issue(@Valid @RequestBody IssueTicketRequest req) {
        return ScanTicketResponse.from(scanTicketService.issueTicket(req));
    }

    @GetMapping("/tickets/{code}")
    public ScanTicketResponse get(@PathVariable String code, Authentication authentication) {
        ScanTicket ticket = ownedTicket(code, authentication);
        return ScanTicketResponse.from(ticket);
    }

    @GetMapping("/users/{userId}/tickets")
    public List<ScanTicketResponse> byUser(@PathVariable Long userId) {
        return scanTicketService.getByUser(userId).stream().map(ScanTicketResponse::from).toList();
    }

    @GetMapping(value = "/tickets/{code}/qr.png", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> qr(@PathVariable String code,
                                     @RequestParam(defaultValue = "300") int size,
                                     Authentication authentication) {
        ownedTicket(code, authentication);
        int s = Math.min(Math.max(size, 100), 1000);
        return png(qrCodeService.generateQr(code, s));
    }

    @GetMapping(value = "/tickets/{code}/barcode.png", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> barcode(@PathVariable String code, Authentication authentication) {
        ownedTicket(code, authentication);
        return png(qrCodeService.generateCode128(code, 450, 140));
    }

    private ResponseEntity<byte[]> png(byte[] data) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .contentType(MediaType.IMAGE_PNG).body(data);
    }

    //Scan

    @PostMapping("/scan")
    public ScanResponse scan(@Valid @RequestBody ScanRequest req, Authentication authentication) {
        return scanTicketService.scan(req.code(), authentication.getName());
    }

    @PostMapping(value = "/scan/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ScanResponse scanImage(@RequestParam("file") MultipartFile file,
                                  Authentication authentication) throws IOException {
        Optional<String> decoded;
        try (InputStream in = file.getInputStream()) {
            decoded = qrCodeService.decode(in);
        }
        if (decoded.isEmpty()) {
            return new ScanResponse(false, ScanResult.INVALID_FORMAT,
                    "Không đọc được mã trong ảnh", null, null, null, null);
        }
        return scanTicketService.scan(decoded.get(), authentication.getName());
    }

    @GetMapping("/scan/logs")
    public List<ScanLogView> logs() {
        return scanTicketService.recentLogs().stream().map(ScanLogView::from).toList();
    }

    public record ScanLogView(String ticketCode, ScanResult result, String scannedBy, String scannedAt) {
        static ScanLogView from(ScanLog l) {
            return new ScanLogView(l.getTicketCode(), l.getResult(), l.getScannedBy(), l.getScannedAt().toString());
        }
    }

    private ScanTicket ownedTicket(String code, Authentication authentication) {
        ScanTicket ticket = scanTicketService.getByCode(code);
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        boolean owner = authentication.getPrincipal() instanceof MemberPrincipal principal
                && ticket.getUserId().equals(principal.getMemberId());
        if (!admin && !owner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return ticket;
    }
}
