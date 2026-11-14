package sum25.se180455.metro.service;

import org.junit.jupiter.api.Test;
import sum25.se180455.metro.config.SePayProperties;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;

class SePayWebhookAuthenticatorTest {
    @Test
    void verifiesHmacRawBodyAndRejectsReplayOrTampering() throws Exception {
        SePayProperties properties = configured("HMAC");
        properties.setWebhookSecret("test-hmac-secret");
        properties.setWebhookMaxAgeSeconds(300);
        SePayWebhookAuthenticator authenticator = new SePayWebhookAuthenticator(properties);
        byte[] body = "{\"id\":92704,\"transferAmount\":300000}".getBytes(StandardCharsets.UTF_8);
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String signature = signature(properties.getWebhookSecret(), timestamp, body);

        assertThat(authenticator.authenticate(body, null, signature, timestamp)).isTrue();
        assertThat(authenticator.authenticate("{}".getBytes(StandardCharsets.UTF_8), null, signature, timestamp)).isFalse();
        assertThat(authenticator.authenticate(body, null, signature,
                String.valueOf(Instant.now().minusSeconds(301).getEpochSecond()))).isFalse();
    }

    @Test
    void verifiesApiKeyInConstantTimeComparisonPath() {
        SePayProperties properties = configured("API_KEY");
        properties.setApiKey("test-api-key");
        SePayWebhookAuthenticator authenticator = new SePayWebhookAuthenticator(properties);

        assertThat(authenticator.authenticate(new byte[0], "Apikey test-api-key", null, null)).isTrue();
        assertThat(authenticator.authenticate(new byte[0], "Apikey wrong", null, null)).isFalse();
    }

    private SePayProperties configured(String mode) {
        SePayProperties properties = new SePayProperties();
        properties.setEnabled(true);
        properties.setBank("Vietcombank");
        properties.setAccountNumber("0123456789");
        properties.setWebhookAuthMode(mode);
        return properties;
    }

    private String signature(String secret, String timestamp, byte[] body) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        mac.update((timestamp + ".").getBytes(StandardCharsets.UTF_8));
        return "sha256=" + HexFormat.of().formatHex(mac.doFinal(body));
    }
}
