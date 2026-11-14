package sum25.se180455.metro.request;

public record SePayWebhookPayload(
        Long id,
        String gateway,
        String transactionDate,
        String accountNumber,
        String subAccount,
        String code,
        String content,
        String transferType,
        String description,
        Long transferAmount,
        Long accumulated,
        String referenceCode) {
}
