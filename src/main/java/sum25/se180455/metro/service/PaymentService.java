package sum25.se180455.metro.service;

import sum25.se180455.metro.pojo.Payment;
import sum25.se180455.metro.request.PaymentRequest;

public interface PaymentService {
    Payment processPayment(PaymentRequest request);

    public void save(Payment payment);
}
