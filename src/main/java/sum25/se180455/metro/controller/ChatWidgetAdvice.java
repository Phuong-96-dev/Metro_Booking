package sum25.se180455.metro.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import sum25.se180455.metro.service.ChatSessionManager;

@ControllerAdvice
public class ChatWidgetAdvice {
    private final ChatSessionManager sessions;
    public ChatWidgetAdvice(ChatSessionManager sessions) { this.sessions = sessions; }
    @ModelAttribute
    public void widget(HttpSession session, Authentication authentication, Model model) {
        model.addAttribute("metroChatSessionKey", sessions.state(session, authentication).key());
    }
}
