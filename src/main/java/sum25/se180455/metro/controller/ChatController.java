package sum25.se180455.metro.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import sum25.se180455.metro.request.*;
import sum25.se180455.metro.service.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final GeminiChatService gemini;
    private final ChatSessionManager sessions;
    private final ChatRateLimiter limiter;
    public ChatController(GeminiChatService gemini, ChatSessionManager sessions, ChatRateLimiter limiter) {
        this.gemini = gemini; this.sessions = sessions; this.limiter = limiter;
    }

    @PostMapping(value = "/message", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ChatResponseDTO> message(@Valid @RequestBody ChatRequestDTO request,
            HttpServletRequest servletRequest, HttpSession session, Authentication authentication) {
        var state = sessions.state(session, authentication);
        limiter.acquire(servletRequest.getRemoteAddr(), state.key());
        start(state);
        try {
            String message = request.message().strip();
            if (message.isBlank()) throw new ChatException(HttpStatus.BAD_REQUEST, "INVALID_MESSAGE", "Vui lòng nhập nội dung tin nhắn.");
            String reply = gemini.reply(message, state.history());
            state.append(message, reply);
            return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new ChatResponseDTO(reply));
        } finally { state.finish(); }
    }

    @PostMapping("/reset")
    public ResponseEntity<Void> reset(HttpSession session, Authentication authentication) {
        var state = sessions.state(session, authentication);
        start(state);
        try { state.clear(); }
        finally { state.finish(); }
        return ResponseEntity.noContent().cacheControl(CacheControl.noStore()).build();
    }
    private void start(ChatSessionManager.State state) {
        if (!state.tryStart()) throw new ChatException(HttpStatus.CONFLICT, "IN_PROGRESS",
                "Metro AI đang trả lời tin nhắn trước. Vui lòng chờ một chút.");
    }
}
