package com.ecommerce.project.controller;


import com.ecommerce.project.payload.PaymentIntentResponse;
import com.ecommerce.project.services.PaymentService;
import com.stripe.exception.StripeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    @Autowired
    private PaymentService paymentService;

    @PostMapping("/{orderId}/intent")
    public ResponseEntity<PaymentIntentResponse> createPaymentIntent(
            @PathVariable Long orderId
    ) throws StripeException {
        return ResponseEntity.ok(paymentService.createPaymentIntent(orderId));

    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebHook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature
    ){
        System.out.println("====== WEBHOOK ENTERED ======");
        System.out.println("Signature "  + signature);
        paymentService.handleWebhook(payload,signature);

        return ResponseEntity.ok("Webhook processed");
    }
}
