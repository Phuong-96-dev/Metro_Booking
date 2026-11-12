package sum25.se180455.metro.request;

import sum25.se180455.metro.pojo.ScanTicket;
import sum25.se180455.metro.pojo.TicketStatus;

import java.time.LocalDateTime;

public record ScanTicketResponse(
        String code, String orderRef, Long userId, String eventName, TicketStatus status,
        LocalDateTime issuedAt, LocalDateTime expiresAt, LocalDateTime usedAt,
        String qrUrl, String barcodeUrl) {

    public static ScanTicketResponse from(ScanTicket t) {
        return new ScanTicketResponse(t.getCode(), t.getOrderRef(), t.getUserId(), t.getEventName(),
                t.getStatus(), t.getIssuedAt(), t.getExpiresAt(), t.getUsedAt(),
                "/api/tickets/" + t.getCode() + "/qr.png",
                "/api/tickets/" + t.getCode() + "/barcode.png");
    }
}
