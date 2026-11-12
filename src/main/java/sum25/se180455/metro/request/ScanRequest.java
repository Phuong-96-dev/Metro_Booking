package sum25.se180455.metro.request;

import jakarta.validation.constraints.NotBlank;

public record ScanRequest(@NotBlank String code, String scannedBy) {
}
