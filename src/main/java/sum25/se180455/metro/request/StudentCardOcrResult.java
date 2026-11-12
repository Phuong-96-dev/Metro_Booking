package sum25.se180455.metro.request;

import java.time.LocalDate;

public record StudentCardOcrResult(
        String imagePath,
        String imageContentType,
        String text,
        String fullName,
        String studentCode,
        String schoolName,
        LocalDate dateOfBirth,
        LocalDate issuedAt,
        LocalDate expiresAt) {
}
