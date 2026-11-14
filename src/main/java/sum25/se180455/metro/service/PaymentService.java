package sum25.se180455.metro.service;

import sum25.se180455.metro.pojo.Payment;
import sum25.se180455.metro.request.PaymentRequest;
import sum25.se180455.metro.request.SePayCheckout;
import sum25.se180455.metro.request.SePayWebhookPayload;

import java.util.Optional;

public interface PaymentService {
    Payment processPayment(PaymentRequest request);

    Payment save(Payment payment);

    SePayCheckout prepareSePayCheckout(Long ticketId, Long memberId);

    Optional<Payment> findOwnedPayment(Long ticketId, Long memberId);

    boolean confirmSePayPayment(SePayWebhookPayload payload);

    boolean isSePayConfigured();
}
