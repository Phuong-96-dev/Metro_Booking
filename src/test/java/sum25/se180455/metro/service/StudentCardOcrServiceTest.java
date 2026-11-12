package sum25.se180455.metro.service;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StudentCardOcrServiceTest {
    @Test
    void extractsLabeledVietnameseStudentCardFields() {
        StudentCardOcrService.ParsedFields result = StudentCardOcrService.parseFields("""
                HỌ VÀ TÊN: Nguyễn Văn An
                MÃ SINH VIÊN: SE180455
                TRƯỜNG: Đại học FPT
                Ngày sinh: 04/05/2004
                Ngày cấp: 01/09/2023
                Hết hạn: 01/09/2027
                """);

        assertEquals("Nguyễn Văn An", result.fullName());
        assertEquals("SE180455", result.studentCode());
        assertEquals("Đại học FPT", result.schoolName());
        assertEquals(LocalDate.of(2004, 5, 4), result.dateOfBirth());
        assertEquals(LocalDate.of(2023, 9, 1), result.issuedAt());
        assertEquals(LocalDate.of(2027, 9, 1), result.expiresAt());
    }

    @Test
    void leavesUnrecognizedFieldsEmptyInsteadOfInventingValues() {
        StudentCardOcrService.ParsedFields result = StudentCardOcrService.parseFields(
                "Some text without student card labels");

        assertEquals("", result.fullName());
        assertEquals("", result.studentCode());
        assertEquals("", result.schoolName());
        assertNull(result.dateOfBirth());
        assertNull(result.issuedAt());
        assertNull(result.expiresAt());
    }

    @Test
    void rejectsNonImageUploadsBeforeStartingOcr() {
        StudentCardOcrService service = new StudentCardOcrService();
        MockMultipartFile upload = new MockMultipartFile(
                "cardImage", "card.png", "image/png", "not an image".getBytes());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> service.process(upload));
        assertEquals("Chỉ chấp nhận ảnh JPG, JPEG, PNG hoặc WEBP.", exception.getMessage());
    }

    @Test
    void rejectsUploadsLargerThanFiveMegabytes() {
        StudentCardOcrService service = new StudentCardOcrService();
        MockMultipartFile upload = new MockMultipartFile(
                "cardImage", "card.png", "image/png", new byte[5 * 1024 * 1024 + 1]);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> service.process(upload));
        assertEquals("Ảnh thẻ không được vượt quá 5 MB.", exception.getMessage());
    }
}
