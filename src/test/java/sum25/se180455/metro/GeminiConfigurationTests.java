package sum25.se180455.metro;

import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import sum25.se180455.metro.config.GeminiConfiguration;
import sum25.se180455.metro.service.ChatException;
import sum25.se180455.metro.service.GeminiChatService;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class GeminiConfigurationTests {
    private final ApplicationContextRunner context = new ApplicationContextRunner()
            .withUserConfiguration(GeminiConfiguration.class)
            .withBean(GeminiChatService.class)
            .withPropertyValues("app.gemini.api-key=", "app.gemini.model=gemini-3.1-flash-lite",
                    "app.gemini.timeout-seconds=25");

    @Test void runtimeProviderIsAvailableAndEnforcesGeminiConstraints() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            assertThat(factory.getClass().getName()).startsWith("org.hibernate.validator.");
            assertThat(factory.getValidator().validate(new GeminiConfiguration.Properties("", "invalid/model", 0)))
                    .extracting(violation -> violation.getPropertyPath().toString())
                    .contains("model", "timeoutSeconds").doesNotContain("apiKey");
        }
    }

    @Test void missingKeyBindsAndStartsButChatReportsNotConfigured() {
        context.run(application -> {
            assertThat(application).hasNotFailed();
            var properties = application.getBean(GeminiConfiguration.Properties.class);
            assertThat(properties.apiKey()).isEmpty();
            assertThat(properties.model()).isEqualTo("gemini-3.1-flash-lite");
            assertThat(properties.timeoutSeconds()).isEqualTo(25);
            assertThatThrownBy(() -> application.getBean(GeminiChatService.class).reply("Xin chào", List.of()))
                    .isInstanceOfSatisfying(ChatException.class, error -> {
                        assertThat(error.status().value()).isEqualTo(503);
                        assertThat(error.code()).isEqualTo("NOT_CONFIGURED");
                    });
        });
    }

    @Test void invalidConfigurationIsStillValidatedDuringBinding() {
        context.withPropertyValues("app.gemini.timeout-seconds=0").run(application -> {
            assertThat(application).hasFailed();
            assertThat(application.getStartupFailure()).hasStackTraceContaining("timeoutSeconds");
        });
    }
}
