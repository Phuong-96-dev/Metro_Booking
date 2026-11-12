package sum25.se180455.metro.request;

import sum25.se180455.metro.pojo.ScanResult;

import java.time.LocalDateTime;

public record ScanResponse(
        boolean success,
        ScanResult result,
        String message,
        String code,
        String eventName,
        Long userId,
        LocalDateTime usedAt) {
}
