package sum25.se180455.metro.request;

import sum25.se180455.metro.pojo.Payment;

public record SePayCheckout(
        Payment payment,
        String qrUrl,
        String bank,
        String accountNumber,
        String accountHolder,
        String transferDescription,
        long amount) {
}
