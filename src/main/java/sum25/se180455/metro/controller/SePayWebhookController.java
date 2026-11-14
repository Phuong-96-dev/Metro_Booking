package sum25.se180455.metro.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sum25.se180455.metro.request.SePayWebhookPayload;
import sum25.se180455.metro.service.PaymentService;
import sum25.se180455.metro.service.SePayWebhookAuthenticator;

import java.util.Map;

@RestController
@RequestMapping("/api/webhooks/sepay")
public class SePayWebhookController {
    private static final Logger log = LoggerFactory.getLogger(SePayWebhookController.class);

    private final ObjectMapper objectMapper;
    private final PaymentService paymentService;
    private final SePayWebhookAuthenticator authenticator;

    public SePayWebhookController(ObjectMapper objectMapper,
                                  PaymentService paymentService,
                                  SePayWebhookAuthenticator authenticator) {
        this.objectMapper = objectMapper;
        this.paymentService = paymentService;
        this.authenticator = authenticator;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> receive(
            @RequestBody byte[] rawBody,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestHeader(value = "X-SePay-Signature", required = false) String signature,
            @RequestHeader(value = "X-SePay-Timestamp", required = false) String timestamp) {
        if (!paymentService.isSePayConfigured()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("success", false, "message", "SePay is not configured"));
        }
        if (!authenticator.authenticate(rawBody, authorization, signature, timestamp)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Unauthorized"));
        }
        SePayWebhookPayload payload;
        try {
            payload = objectMapper.readValue(rawBody, SePayWebhookPayload.class);
        } catch (Exception exception) {
            log.warn("Invalid SePay webhook JSON");
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Invalid payload"));
        }
        try {
            boolean applied = paymentService.confirmSePayPayment(payload);
            if (!applied) {
                log.warn("Ignored SePay transaction id={} code={} type={} amount={}",
                        payload.id(), payload.code(), payload.transferType(), payload.transferAmount());
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("success", true));
        } catch (RuntimeException exception) {
            log.error("Failed to process SePay transaction id={}", payload.id(), exception);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Processing failed"));
        }
    }
}
