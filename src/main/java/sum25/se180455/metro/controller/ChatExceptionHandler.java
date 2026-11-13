package sum25.se180455.metro.controller;

import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import sum25.se180455.metro.service.ChatException;
import java.util.Map;

@RestControllerAdvice(assignableTypes = ChatController.class)
public class ChatExceptionHandler {
    @ExceptionHandler(ChatException.class)
    ResponseEntity<Map<String, String>> chat(ChatException exception) {
        var response = ResponseEntity.status(exception.status()).cacheControl(CacheControl.noStore());
        if (exception.status() == HttpStatus.TOO_MANY_REQUESTS) response.header(HttpHeaders.RETRY_AFTER, "60");
        return response.body(Map.of("code", exception.code(), "error", exception.getMessage()));
    }
    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    ResponseEntity<Map<String, String>> invalid(Exception exception) {
        return ResponseEntity.badRequest().cacheControl(CacheControl.noStore()).body(Map.of("code", "INVALID_MESSAGE",
                "error", "Vui lòng gửi tin nhắn từ 1 đến 2.000 ký tự."));
    }
    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String, String>> unexpected(Exception exception) {
        return ResponseEntity.internalServerError().cacheControl(CacheControl.noStore()).body(Map.of("code", "INTERNAL_ERROR",
                "error", "Metro AI gặp sự cố. Vui lòng thử lại sau."));
    }
}
