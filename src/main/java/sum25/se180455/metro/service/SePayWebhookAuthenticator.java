package sum25.se180455.metro.service;

import org.springframework.stereotype.Service;
import sum25.se180455.metro.config.SePayProperties;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;

@Service
public class SePayWebhookAuthenticator {
    private final SePayProperties properties;

    public SePayWebhookAuthenticator(SePayProperties properties) {
        this.properties = properties;
    }

    public boolean authenticate(byte[] rawBody, String authorization, String signature, String timestampValue) {
        if (!properties.isConfigured()) {
            return false;
        }
        if ("API_KEY".equals(properties.authMode())) {
            return constantTimeEquals("Apikey " + properties.getApiKey(), authorization);
        }
        if (!"HMAC".equals(properties.authMode())) {
            return false;
        }
        try {
            long timestamp = Long.parseLong(timestampValue);
            long age = Math.abs(Instant.now().getEpochSecond() - timestamp);
            if (age > Math.max(1, properties.getWebhookMaxAgeSeconds())) {
                return false;
            }
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(properties.getWebhookSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            mac.update((timestamp + ".").getBytes(StandardCharsets.UTF_8));
            String expected = "sha256=" + HexFormat.of().formatHex(mac.doFinal(rawBody));
            return constantTimeEquals(expected, signature);
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean constantTimeEquals(String expected, String actual) {
        if (expected == null || actual == null) {
            return false;
        }
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8));
    }
}
