package sum25.se180455.metro.service;

import org.springframework.http.HttpStatus;

/** Public messages only: never propagate provider bodies, credentials or stack traces. */
public class ChatException extends RuntimeException {
    private final HttpStatus status;
    private final String code;
    public ChatException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }
    public HttpStatus status() { return status; }
    public String code() { return code; }
}
