package sum25.se180455.metro.service;

import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sum25.se180455.metro.request.StudentCardOcrResult;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Iterator;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class StudentCardOcrService {
    private static final Logger logger = LoggerFactory.getLogger(StudentCardOcrService.class);
    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;
    private static final long MAX_IMAGE_PIXELS = 12_000_000L;
    private static final Pattern DATE_PATTERN = Pattern.compile("\\b(\\d{1,2}[/-]\\d{1,2}[/-]\\d{4})\\b");

    @Value("${student-verification.storage-directory:${user.home}/metro-private/student-verification}")
    private String storageDirectory;

    @Value("${student-verification.ocr-command:tesseract}")
    private String tesseractCommand;

    @Value("${student-verification.ocr-languages:vie+eng}")
    private String languages;

    public StudentCardOcrResult process(MultipartFile upload) {
        if (upload == null || upload.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn ảnh thẻ sinh viên.");
        }
        if (upload.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Ảnh thẻ không được vượt quá 5 MB.");
        }

        Path directory;
        Path inputPath = null;
        Path pngPath = null;
        Path outputPath = null;
        Path errorPath = null;
        Process process = null;
        try {
            byte[] imageBytes = upload.getBytes();
            BufferedImage image = readSupportedImage(imageBytes);
            String format = findFormat(imageBytes);
            directory = Path.of(storageDirectory).toAbsolutePath().normalize();
            Files.createDirectories(directory);
            String extension = format.equals("jpeg") ? "jpg" : format;
            String contentType = format.equals("jpeg") || format.equals("jpg") ? "image/jpeg" : "image/" + format;
            String token = UUID.randomUUID().toString();
            inputPath = Files.createTempFile(directory, "student-card-", "." + extension);
            pngPath = Files.createTempFile(directory, "student-card-ocr-", ".png");
            outputPath = Files.createTempFile(directory, "student-card-ocr-", ".txt");
            errorPath = Files.createTempFile(directory, "student-card-ocr-", ".err");
            Files.write(inputPath, imageBytes);
            if (!ImageIO.write(image, "png", pngPath.toFile())) {
                throw new IllegalStateException("Không thể chuẩn bị ảnh để nhận diện.");
            }

            process = new ProcessBuilder(
                    tesseractCommand, pngPath.toString(), "stdout", "-l", languages, "--psm", "6")
                    .redirectOutput(outputPath.toFile())
                    .redirectError(errorPath.toFile())
                    .start();
            if (!process.waitFor(30, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                process.waitFor();
                throw new IllegalStateException("OCR mất quá nhiều thời gian. Vui lòng thử ảnh khác.");
            }
            if (process.exitValue() != 0) {
                throw new IllegalStateException("Không thể xử lý ảnh bằng OCR. Vui lòng kiểm tra cấu hình Tesseract.");
            }
            if (Files.size(outputPath) > 100_000) {
                throw new IllegalArgumentException("Ảnh tạo ra quá nhiều nội dung OCR. Vui lòng dùng ảnh khác.");
            }
            String text = Files.readString(outputPath).trim();
            if (text.isBlank()) {
                throw new IllegalArgumentException("OCR không đọc được nội dung. Vui lòng tải ảnh rõ nét hơn.");
            }

            Path storedPath = directory.resolve(token + "." + extension);
            Files.move(inputPath, storedPath);
            inputPath = null;
            ParsedFields fields = parseFields(text);
            return new StudentCardOcrResult(
                    storedPath.toString(), contentType, text,
                    fields.fullName(), fields.studentCode(), fields.schoolName(),
                    fields.dateOfBirth(), fields.issuedAt(), fields.expiresAt());
        } catch (IOException e) {
            throw new IllegalStateException("Không thể đọc hoặc lưu ảnh thẻ. Vui lòng thử lại.", e);
        } catch (InterruptedException e) {
            if (process != null) {
                process.destroyForcibly();
            }
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Quá trình OCR đã bị gián đoạn.", e);
        } finally {
            deleteQuietly(inputPath);
            deleteQuietly(pngPath);
            deleteQuietly(outputPath);
            deleteQuietly(errorPath);
        }
    }

    public void deleteImage(String imagePath) {
        if (imagePath == null) {
            return;
        }
        Path directory = Path.of(storageDirectory).toAbsolutePath().normalize();
        Path file = Path.of(imagePath).toAbsolutePath().normalize();
        if (!file.startsWith(directory)) {
            throw new IllegalArgumentException("Đường dẫn ảnh không hợp lệ.");
        }
        deleteQuietly(file);
    }

    public byte[] readImage(String imagePath) throws IOException {
        Path directory = Path.of(storageDirectory).toAbsolutePath().normalize();
        Path file = Path.of(imagePath).toAbsolutePath().normalize();
        if (!file.startsWith(directory) || !Files.isRegularFile(file)) {
            throw new FileNotFoundException("Student verification image not found.");
        }
        return Files.readAllBytes(file);
    }

    private BufferedImage readSupportedImage(byte[] bytes) throws IOException {
        try (ImageInputStream stream = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            if (stream == null) {
                throw new IllegalArgumentException("Tệp tải lên không phải ảnh hợp lệ.");
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
            if (!readers.hasNext()) {
                throw new IllegalArgumentException("Chỉ chấp nhận ảnh JPG, JPEG, PNG hoặc WEBP.");
            }
            ImageReader reader = readers.next();
            try {
                String format = reader.getFormatName().toLowerCase(Locale.ROOT);
                if (!format.equals("jpeg") && !format.equals("jpg")
                        && !format.equals("png") && !format.equals("webp")) {
                    throw new IllegalArgumentException("Chỉ chấp nhận ảnh JPG, JPEG, PNG hoặc WEBP.");
                }
                reader.setInput(stream);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width <= 0 || height <= 0 || (long) width * height > MAX_IMAGE_PIXELS) {
                    throw new IllegalArgumentException("Kích thước ảnh không hợp lệ hoặc quá lớn.");
                }
                BufferedImage image = reader.read(0);
                if (image == null) {
                    throw new IllegalArgumentException("Tệp tải lên không phải ảnh hợp lệ.");
                }
                return image;
            } finally {
                reader.dispose();
            }
        }
    }

    private String findFormat(byte[] bytes) throws IOException {
        try (ImageInputStream stream = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
            if (!readers.hasNext()) {
                throw new IllegalArgumentException("Tệp tải lên không phải ảnh hợp lệ.");
            }
            ImageReader reader = readers.next();
            try {
                return reader.getFormatName().toLowerCase(Locale.ROOT);
            } finally {
                reader.dispose();
            }
        }
    }

    static ParsedFields parseFields(String text) {
        return new ParsedFields(
                limit(labeledValue(text, "(?:họ\\s*và\\s*tên|họ\\s*tên|full\\s*name|name)"), 150),
                limit(labeledValue(text, "(?:mã\\s*(?:sinh\\s*viên|sv|thẻ)|student\\s*(?:id|no\\.?))"), 100),
                limit(labeledValue(text, "(?:trường|đại\\s*học|university|college)"), 200),
                labeledDate(text, "(?:ngày\\s*sinh|date\\s*of\\s*birth|dob)"),
                labeledDate(text, "(?:ngày\\s*cấp|issued)"),
                labeledDate(text, "(?:hết\\s*hạn|ngày\\s*hết\\s*hạn|expiry|valid\\s*until)"));
    }

    private static String labeledValue(String text, String labels) {
        Pattern pattern = Pattern.compile(
                "^\\s*" + labels + "\\s*[:\\-]?\\s*(.+?)\\s*$",
                Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(text);
        if (!matcher.find()) {
            return "";
        }
        return matcher.group(1).replaceAll("\\s{2,}", " ").trim();
    }

    private static LocalDate labeledDate(String text, String labels) {
        String value = labeledValue(text, labels);
        Matcher matcher = DATE_PATTERN.matcher(value);
        if (!matcher.find()) {
            return null;
        }
        String date = matcher.group(1).replace('-', '/');
        for (DateTimeFormatter formatter : new DateTimeFormatter[]{
                DateTimeFormatter.ofPattern("d/M/uuuu"),
                DateTimeFormatter.ofPattern("dd/MM/uuuu")}) {
            try {
                return LocalDate.parse(date, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }

    private void deleteQuietly(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            logger.warn("Unable to remove a temporary student verification file.");
        }
    }

    private static String limit(String value, int maxLength) {
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    record ParsedFields(
            String fullName,
            String studentCode,
            String schoolName,
            LocalDate dateOfBirth,
            LocalDate issuedAt,
            LocalDate expiresAt) {
    }
}
