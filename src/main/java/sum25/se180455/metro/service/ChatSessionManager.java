package sum25.se180455.metro.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class ChatSessionManager {
    private static final String ATTRIBUTE = ChatSessionManager.class.getName();
    public State state(HttpSession session, Authentication authentication) {
        String owner = authentication == null || authentication instanceof AnonymousAuthenticationToken
                ? "anonymous" : "authenticated:" + authentication.getName();
        synchronized (session) {
            State state = (State) session.getAttribute(ATTRIBUTE);
            if (state == null || !state.owner.equals(owner)) {
                state = new State(owner);
                session.setAttribute(ATTRIBUTE, state);
            }
            return state;
        }
    }

    public record Turn(String role, String text) { }
    public static final class State {
        private final String owner;
        private final String key = UUID.randomUUID().toString();
        private final List<Turn> history = new ArrayList<>();
        private final AtomicBoolean busy = new AtomicBoolean();
        private State(String owner) { this.owner = owner; }
        public String key() { return key; }
        public boolean tryStart() { return busy.compareAndSet(false, true); }
        public void finish() { busy.set(false); }
        public synchronized List<Turn> history() { return List.copyOf(history); }
        public synchronized void append(String message, String reply) {
            history.add(new Turn("user", message));
            history.add(new Turn("model", reply));
            while (history.size() > 12) history.subList(0, 2).clear();
        }
        public synchronized void clear() { history.clear(); }
    }
}
