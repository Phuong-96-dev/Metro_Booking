package sum25.se180455.metro.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@ConfigurationProperties(prefix = "app.sepay")
public class SePayProperties {
    private boolean enabled;
    private String bank = "";
    private String accountNumber = "";
    private String accountHolder = "";
    private String paymentPrefix = "METRO";
    private String transferPrefix = "";
    private String webhookAuthMode = "HMAC";
    private String webhookSecret = "";
    private String apiKey = "";
    private long webhookMaxAgeSeconds = 300;

    public boolean isConfigured() {
        if (!enabled || blank(bank) || blank(accountNumber)) {
            return false;
        }
        return "API_KEY".equals(authMode()) ? !blank(apiKey) : !blank(webhookSecret);
    }

    public String authMode() {
        return webhookAuthMode == null ? "HMAC" : webhookAuthMode.strip().toUpperCase(Locale.ROOT);
    }

    public String normalizedPaymentPrefix() {
        String normalized = paymentPrefix == null ? "" : paymentPrefix.toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]", "");
        return normalized.isBlank() ? "METRO" : normalized.substring(0, Math.min(normalized.length(), 16));
    }

    public String transferDescription(String transactionCode) {
        String prefix = transferPrefix == null ? "" : transferPrefix.strip();
        return prefix.isBlank() ? transactionCode : prefix + " " + transactionCode;
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getBank() { return bank; }
    public void setBank(String bank) { this.bank = bank; }
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public String getAccountHolder() { return accountHolder; }
    public void setAccountHolder(String accountHolder) { this.accountHolder = accountHolder; }
    public String getPaymentPrefix() { return paymentPrefix; }
    public void setPaymentPrefix(String paymentPrefix) { this.paymentPrefix = paymentPrefix; }
    public String getTransferPrefix() { return transferPrefix; }
    public void setTransferPrefix(String transferPrefix) { this.transferPrefix = transferPrefix; }
    public String getWebhookAuthMode() { return webhookAuthMode; }
    public void setWebhookAuthMode(String webhookAuthMode) { this.webhookAuthMode = webhookAuthMode; }
    public String getWebhookSecret() { return webhookSecret; }
    public void setWebhookSecret(String webhookSecret) { this.webhookSecret = webhookSecret; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public long getWebhookMaxAgeSeconds() { return webhookMaxAgeSeconds; }
    public void setWebhookMaxAgeSeconds(long webhookMaxAgeSeconds) { this.webhookMaxAgeSeconds = webhookMaxAgeSeconds; }
}
