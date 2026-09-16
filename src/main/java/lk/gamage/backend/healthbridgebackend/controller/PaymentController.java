package lk.gamage.backend.healthbridgebackend.controller;

import jakarta.validation.Valid;
import lk.gamage.backend.healthbridgebackend.dto.PaymentConfirmRequest;
import lk.gamage.backend.healthbridgebackend.dto.PaymentRequest;
import lk.gamage.backend.healthbridgebackend.model.Payment;
import lk.gamage.backend.healthbridgebackend.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    /**
     * POST /api/payments/initiate
     * Patient submits payment details + credit card info.
     * A 6-digit confirmation code is sent to their registered email.
     */
    @PostMapping("/initiate")
    public ResponseEntity<?> initiatePayment(@Valid @RequestBody PaymentRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));
            return ResponseEntity.badRequest().body(Map.of("message", "Validation failed", "errors", errors));
        }

        try {
            Payment payment = paymentService.initiatePayment(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Payment initiated. A 6-digit confirmation code has been sent to your registered email.",
                    "paymentId", payment.getId(),
                    "maskedCard", payment.getMaskedCardNumber(),
                    "amount", payment.getAmount(),
                    "status", payment.getStatus()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "An unexpected error occurred while processing payment"));
        }
    }

    /**
     * POST /api/payments/{id}/confirm
     * Patient enters the 6-digit code to confirm payment.
     */
    @PostMapping("/{id}/confirm")
    public ResponseEntity<?> confirmPayment(@PathVariable String id,
                                             @Valid @RequestBody PaymentConfirmRequest request,
                                             BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Please enter a valid 6-digit confirmation code"));
        }

        try {
            Payment payment = paymentService.confirmPayment(id, request.getConfirmationCode());
            return ResponseEntity.ok(Map.of(
                    "message", "Payment confirmed successfully!",
                    "paymentId", payment.getId(),
                    "status", payment.getStatus(),
                    "confirmedAt", payment.getConfirmedAt().toString(),
                    "amount", payment.getAmount()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Payment confirmation failed"));
        }
    }

    /**
     * GET /api/payments/patient/{patientId}
     * Get all payments for a patient.
     */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<?> getPaymentsByPatient(@PathVariable String patientId) {
        try {
            List<Payment> payments = paymentService.getPaymentsByPatient(patientId);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to retrieve payments"));
        }
    }

    /**
     * GET /api/payments/{id}
     * Get a single payment detail.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getPaymentById(@PathVariable String id) {
        try {
            Payment payment = paymentService.getPaymentById(id);
            return ResponseEntity.ok(payment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to retrieve payment"));
        }
    }
}
