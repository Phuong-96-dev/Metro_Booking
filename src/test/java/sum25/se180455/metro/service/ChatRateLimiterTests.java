package sum25.se180455.metro.service;

import org.junit.jupiter.api.Test;
import java.time.*;
import static org.assertj.core.api.Assertions.*;

class ChatRateLimiterTests {
    @Test void limitsIpAcrossFreshSessionsAndReleasesAfterWindow() {
        var clock = org.mockito.Mockito.mock(Clock.class);
        org.mockito.Mockito.when(clock.millis()).thenReturn(1000L);
        var limiter = new ChatRateLimiter(clock);
        for (int i = 0; i < 20; i++) limiter.acquire("same-ip", "session-" + i);
        assertThatThrownBy(() -> limiter.acquire("same-ip", "new-session")).isInstanceOf(ChatException.class);
        org.mockito.Mockito.when(clock.millis()).thenReturn(61_001L);
        assertThatCode(() -> limiter.acquire("same-ip", "new-session")).doesNotThrowAnyException();
    }
    @Test void limitsGlobalCallsAcrossIps() {
        var limiter = new ChatRateLimiter(Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
        for (int i = 0; i < 120; i++) limiter.acquire("ip-" + i, "session-" + i);
        assertThatThrownBy(() -> limiter.acquire("ip-extra", "session-extra")).isInstanceOf(ChatException.class);
    }
}
