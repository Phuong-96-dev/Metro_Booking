package sum25.se180455.metro;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.*;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import sum25.se180455.metro.config.GeminiConfiguration;
import sum25.se180455.metro.service.*;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class GeminiChatServiceTests {
    MockRestServiceServer server;
    GeminiChatService service;
    @BeforeEach void setup() {
        var builder = RestClient.builder().baseUrl("https://generativelanguage.googleapis.com/v1");
        server = MockRestServiceServer.bindTo(builder).build();
        service = new GeminiChatService(builder.build(), new GeminiConfiguration.Properties("test-only-secret", "gemini-3.1-flash-lite", 25));
    }
    @Test void sendsOfficialRequestAndUsesOnlyVisibleText() {
        server.expect(requestTo("https://generativelanguage.googleapis.com/v1/models/gemini-3.1-flash-lite:generateContent"))
                .andExpect(method(HttpMethod.POST)).andExpect(header("x-goog-api-key", "test-only-secret"))
                .andExpect(jsonPath("$.systemInstruction.parts[0].text").value(org.hamcrest.Matchers.containsString("chưa được cấp tuyến")))
                .andExpect(jsonPath("$.contents[0].role").value("user"))
                .andExpect(jsonPath("$.contents[1].role").value("model"))
                .andExpect(jsonPath("$.contents[2].parts[0].text").value("Mua vé?"))
                .andExpect(jsonPath("$.tools").doesNotExist())
                .andRespond(withSuccess("""
                        {"candidates":[{"finishReason":"STOP","content":{"parts":[
                        {"thought":true,"text":"private thought"},{"text":"Mở /ticket"},{"text":" để xem vé."}]}}]}
                        """, MediaType.APPLICATION_JSON));
        assertThat(service.reply("Mua vé?", List.of(new ChatSessionManager.Turn("user", "Chào"),
                new ChatSessionManager.Turn("model", "Xin chào")))).isEqualTo("Mở /ticket để xem vé.");
        server.verify();
    }
    @ParameterizedTest
    @CsvSource({"429,429,PROVIDER_LIMIT", "404,503,MODEL_UNAVAILABLE", "400,503,MODEL_UNAVAILABLE",
            "401,503,CONFIGURATION_ERROR", "403,503,CONFIGURATION_ERROR", "500,502,PROVIDER_ERROR", "503,502,PROVIDER_ERROR"})
    void mapsProviderErrorsWithoutLeakingBody(int upstream, int downstream, String code) {
        server.expect(anything()).andRespond(withStatus(HttpStatus.valueOf(upstream)).body("secret provider payload test-only-secret"));
        assertThatThrownBy(() -> service.reply("Chào", List.of())).isInstanceOfSatisfying(ChatException.class, error -> {
            assertThat(error.status().value()).isEqualTo(downstream);
            assertThat(error.code()).isEqualTo(code);
            assertThat(error.getMessage()).doesNotContain("test-only-secret", "payload");
        });
        server.verify();
    }
    @Test void handlesNetworkTimeout() {
        server.expect(anything()).andRespond(withException(new java.net.SocketTimeoutException("sensitive network detail")));
        assertThatThrownBy(() -> service.reply("Chào", List.of())).isInstanceOfSatisfying(ChatException.class,
                error -> { assertThat(error.status()).isEqualTo(HttpStatus.GATEWAY_TIMEOUT);
                    assertThat(error.getMessage()).doesNotContain("sensitive"); });
    }
    @Test void missingKeyDoesNotMakeNetworkCall() {
        var missing = new GeminiChatService(RestClient.create(), new GeminiConfiguration.Properties("", "gemini-3.1-flash-lite", 25));
        assertThatThrownBy(() -> missing.reply("Chào", List.of())).isInstanceOfSatisfying(ChatException.class,
                error -> assertThat(error.code()).isEqualTo("NOT_CONFIGURED"));
        server.verify();
    }
    @ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {"{}", "{\"promptFeedback\":{\"blockReason\":\"SAFETY\"}}",
            "{\"candidates\":[{\"finishReason\":\"SAFETY\",\"content\":{\"parts\":[{\"text\":\"blocked\"}]}}]}",
            "{\"candidates\":[{\"finishReason\":\"MAX_TOKENS\"}]}", "not JSON"})
    void rejectsBlockedEmptyAndMalformedResponses(String response) {
        server.expect(anything()).andRespond(withSuccess(response, MediaType.APPLICATION_JSON));
        assertThatThrownBy(() -> service.reply("Chào", List.of())).isInstanceOfSatisfying(ChatException.class,
                error -> assertThat(error.code()).isEqualTo("NO_ANSWER"));
    }
}
