package sum25.se180455.metro.config;

import jakarta.validation.constraints.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
@EnableConfigurationProperties(GeminiConfiguration.Properties.class)
public class GeminiConfiguration {
    @Validated
    @ConfigurationProperties("app.gemini")
    public record Properties(String apiKey,
            @NotBlank @Pattern(regexp = "gemini-[a-z0-9.-]+") String model,
            @Min(1) @Max(60) int timeoutSeconds) { }

    @Bean("geminiRestClient")
    RestClient geminiRestClient(Properties properties) {
        var client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NEVER).build();
        var factory = new JdkClientHttpRequestFactory(client);
        factory.setReadTimeout(Duration.ofSeconds(properties.timeoutSeconds()));
        return RestClient.builder().baseUrl("https://generativelanguage.googleapis.com/v1")
                .requestFactory(factory).build();
    }
}
