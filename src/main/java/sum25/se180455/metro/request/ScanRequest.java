package sum25.se180455.metro.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ScanRequest(@NotBlank @Size(max = 100) String code) {
}
