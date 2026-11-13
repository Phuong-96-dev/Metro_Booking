package sum25.se180455.metro.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.time.Clock;
import java.util.*;

/** Single-instance limiter. Forwarded headers are deliberately not trusted. */
public class AuthRateLimitFilter extends OncePerRequestFilter {
    private record Window(long expires, int count) { }
    private final Map<String, Window> windows = new HashMap<>();
    private final Clock clock;
    public AuthRateLimitFilter() { this(Clock.systemUTC()); }
    public AuthRateLimitFilter(Clock clock) { this.clock = clock; }
    private synchronized boolean allow(String key, int limit) {
        long now = clock.millis();
        windows.entrySet().removeIf(entry -> entry.getValue().expires() <= now);
        Window window = windows.get(key);
        if (window == null && windows.size() >= 10000) return false;
        if (window == null) window = new Window(now + 15 * 60 * 1000L, 0);
        if (window.count() >= limit) return false;
        windows.put(key, new Window(window.expires(), window.count() + 1));
        return true;
    }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                               FilterChain chain) throws ServletException, IOException {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if ("POST".equals(request.getMethod()) && Set.of("/login", "/register", "/account/google/link").contains(path)) {
            String email = Optional.ofNullable(request.getParameter("email")).orElse("").strip().toLowerCase(Locale.ROOT);
            if (!allow("ip:" + request.getRemoteAddr(), 30) || (!email.isEmpty() && email.length() <= 254 && !allow("email:" + email, 10))) {
                response.setStatus(429); response.setHeader("Retry-After", "900");
                response.setContentType("text/plain;charset=UTF-8");
                response.getWriter().write("Bạn đã thử quá nhiều lần. Vui lòng thử lại sau 15 phút.");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
