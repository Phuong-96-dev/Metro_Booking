package sum25.se180455.metro.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import java.time.Clock;
import java.util.HashMap;
import java.util.Map;

/** Per-process limits. IP comes from remoteAddr, never untrusted forwarding headers. */
@Component
public class ChatRateLimiter {
    private final Clock clock;
    private final Map<String, Bucket> buckets = new HashMap<>();
    public ChatRateLimiter() { this(Clock.systemUTC()); }
    ChatRateLimiter(Clock clock) { this.clock = clock; }

    public synchronized void acquire(String address, String sessionKey) {
        long now = clock.millis();
        buckets.entrySet().removeIf(entry -> entry.getValue().expires <= now);
        check("global", 120, now);
        check("ip:" + address, 20, now);
        check("session:" + sessionKey, 10, now);
        increment("global", now);
        increment("ip:" + address, now);
        increment("session:" + sessionKey, now);
    }
    private void check(String key, int limit, long now) {
        Bucket bucket = buckets.get(key);
        if ((bucket != null && bucket.count >= limit) || (bucket == null && buckets.size() >= 9997)) {
            throw new ChatException(HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMITED",
                    "Bạn gửi tin nhắn hơi nhanh. Vui lòng chờ khoảng một phút rồi thử lại.");
        }
    }
    private void increment(String key, long now) {
        buckets.computeIfAbsent(key, ignored -> new Bucket(now + 60_000)).count++;
    }
    private static final class Bucket {
        final long expires;
        int count;
        Bucket(long expires) { this.expires = expires; }
    }
}
