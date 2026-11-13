package sum25.se180455.metro.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatRequestDTO(
        @NotBlank @Size(max = 2000) String message) { }
