package sum25.se180455.metro.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record IssueTicketRequest(
        @NotNull Long userId,
        @NotBlank String orderRef,
        @NotBlank String eventName,
        LocalDateTime expiresAt) {
}
