package lk.gamage.backend.healthbridgebackend.service;

// ✅ CORRECT IMPORTS
import lk.gamage.backend.healthbridgebackend.dto.PaymentRequest;
import lk.gamage.backend.healthbridgebackend.dto.PaymentResponse;   // ✅ FIXED (was dto.response)
import lk.gamage.backend.healthbridgebackend.model.Payment;
import lk.gamage.backend.healthbridgebackend.model.User;
import lk.gamage.backend.healthbridgebackend.repository.PaymentRepository;
import lk.gamage.backend.healthbridgebackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final BillingIntegrationService billingIntegrationService;
    private final lk.gamage.backend.healthbridgebackend.repository.AppointmentRepository appointmentRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    // ============================================================
    // INITIATE PAYMENT
    // ============================================================
    public Payment initiatePayment(PaymentRequest request) {
        User patient = null;
        if (request.getPatientId() != null && !request.getPatientId().isBlank()) {
            patient = userRepository.findById(request.getPatientId().trim()).orElse(null);
            if (patient == null) {
                patient = userRepository.findByEmail(request.getPatientId().trim().toLowerCase()).orElse(null);
            }
        }

        String targetEmail = null;
        if (request.getPatientEmail() != null && !request.getPatientEmail().isBlank()) {
            targetEmail = request.getPatientEmail().trim().toLowerCase();
        } else if (request.getEmail() != null && !request.getEmail().isBlank()) {
            targetEmail = request.getEmail().trim().toLowerCase();
        }

        if (patient == null && targetEmail != null && !targetEmail.isBlank()) {
            patient = userRepository.findByEmail(targetEmail).orElse(null);
        }

        if (patient != null && (targetEmail == null || targetEmail.isBlank())) {
            targetEmail = patient.getEmail();
        }

        if (targetEmail != null) {
            targetEmail = targetEmail.trim().toLowerCase();
        }

        if (patient == null && (targetEmail == null || targetEmail.isBlank())) {
            throw new IllegalArgumentException("Patient not found");
        }

        String cleanCardNumber = request.getCardNumber().replaceAll("[\\s-]", "");
        if (!cleanCardNumber.matches("\\d{13,19}")) {
            throw new IllegalArgumentException("Invalid card number format");
        }
        if (!request.getExpiryDate().matches("(0[1-9]|1[0-2])/\\d{2}")) {
            throw new IllegalArgumentException("Invalid expiry date format. Use MM/YY");
        }
        if (!request.getCvv().matches("\\d{3,4}")) {
            throw new IllegalArgumentException("Invalid CVV format");
        }

        String lastFour = cleanCardNumber.substring(cleanCardNumber.length() - 4);
        String maskedCard = "****-****-****-" + lastFour;

        int code = 100000 + secureRandom.nextInt(900000);
        String confirmationCode = String.valueOf(code);

        String doctorId = request.getDoctorId();
        String doctorName = request.getDoctorName();
        if ((doctorId == null || doctorId.isBlank()) && "CONSULTATION".equalsIgnoreCase(request.getCategory()) && patient != null) {
            try {
                var appointments = appointmentRepository.findByPatientIdOrderByAppointmentDateDescAppointmentTimeDesc(patient.getId());
                if (appointments != null && !appointments.isEmpty()) {
                    var recent = appointments.get(0);
                    doctorId = recent.getDoctorId();
                    doctorName = recent.getDoctorName();
                }
            } catch (Exception ignored) {}
        }
        if (doctorId != null && (doctorName == null || doctorName.isBlank())) {
            doctorName = userRepository.findById(doctorId).map(User::getFullName).orElse(null);
        }

        String patientId = patient != null ? patient.getId() : request.getPatientId();
        String patientName = patient != null ? patient.getFullName() : request.getCardHolderName();

        Payment payment = Payment.builder()
                .patientId(patientId)
                .patientEmail(targetEmail)
                .patientName(patientName)
                .doctorId(doctorId)
                .doctorName(doctorName)
                .description(request.getDescription())
                .category(request.getCategory() != null ? request.getCategory() : "OTHER")
                .amount(request.getAmount())
                .cardHolderName(request.getCardHolderName())
                .maskedCardNumber(maskedCard)
                .confirmationCode(confirmationCode)
                .codeExpiresAt(LocalDateTime.now().plusMinutes(10))
                .status("PENDING_CONFIRMATION")
                .createdAt(LocalDateTime.now())
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        log.info("[Payment] Initiated {} for {} ({}), amount: {}",
                savedPayment.getId(), patientName,
                targetEmail, request.getAmount());

        boolean emailSent = false;
        if (targetEmail != null && !targetEmail.isBlank()) {
            // Use the same technique that is used in forgot-password
            emailSent = emailService.sendOtpEmail(targetEmail, confirmationCode);
            if (!emailSent) {
                // Fallback to payment confirmation email
                emailSent = emailService.sendPaymentConfirmationEmail(
                        targetEmail,
                        confirmationCode,
                        request.getAmount().toPlainString(),
                        request.getDescription()
                );
            }
        }
        savedPayment.setEmailSent(emailSent);
        return paymentRepository.save(savedPayment);
    }

    // ============================================================
    // CONFIRM PAYMENT (with code)
    // ============================================================
    public Payment confirmPayment(String paymentId, String confirmationCode) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        if ("CONFIRMED".equals(payment.getStatus())) {
            throw new IllegalArgumentException("This payment has already been confirmed");
        }

        if ("EXPIRED".equals(payment.getStatus()) ||
                payment.getCodeExpiresAt().isBefore(LocalDateTime.now())) {
            payment.setStatus("EXPIRED");
            paymentRepository.save(payment);
            throw new IllegalArgumentException("Confirmation code has expired.");
        }

        if (!payment.getConfirmationCode().equals(confirmationCode.trim())) {
            throw new IllegalArgumentException("Invalid confirmation code");
        }

        payment.setStatus("CONFIRMED");
        payment.setConfirmedAt(LocalDateTime.now());
        payment.setConfirmationCode(null);

        Payment confirmedPayment = paymentRepository.save(payment);
        log.info("[Payment] {} CONFIRMED for {}", paymentId, payment.getPatientEmail());

        // ✅ Notify Billing Service
        try {
            billingIntegrationService.onPaymentConfirmed(
                    confirmedPayment.getPatientId(),
                    confirmedPayment.getAmount(),
                    paymentId
            );
            log.info("[Payment → Billing] Notified for patient {}",
                    confirmedPayment.getPatientId());
        } catch (Exception e) {
            log.error("[Payment → Billing] Failed: {}", e.getMessage());
        }

        return confirmedPayment;
    }

    // ============================================================
    // SIMPLE CONFIRM
    // ============================================================
    public Payment confirm(String paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        if (!"CONFIRMED".equals(payment.getStatus())) {
            payment.setStatus("CONFIRMED");
            payment.setConfirmedAt(LocalDateTime.now());
            payment.setConfirmationCode(null);
            payment = paymentRepository.save(payment);

            try {
                billingIntegrationService.onPaymentConfirmed(
                        payment.getPatientId(),
                        payment.getAmount(),
                        paymentId
                );
            } catch (Exception e) {
                log.error("[Payment → Billing] Notification failed: {}", e.getMessage());
            }
        }

        return payment;
    }

    // ============================================================
    // CANCEL
    // ============================================================
    public Payment cancel(String paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
        payment.setStatus("CANCELLED");
        return paymentRepository.save(payment);
    }

    // ============================================================
    // GET Methods
    // ============================================================
    public List<Payment> getPaymentsByPatient(String patientId) {
        return paymentRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
    }

    public Payment getPaymentById(String paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    // ============================================================
    // ✅ PUBLIC MAPPER (not private)
    // ============================================================
    public PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .patientId(payment.getPatientId())
                .patientName(payment.getPatientName())
                .patientEmail(payment.getPatientEmail())
                .doctorId(payment.getDoctorId())
                .doctorName(payment.getDoctorName())
                .description(payment.getDescription())
                .category(payment.getCategory())
                .amount(payment.getAmount())
                .cardHolderName(payment.getCardHolderName())
                .maskedCardNumber(payment.getMaskedCardNumber())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .confirmedAt(payment.getConfirmedAt())
                .build();
    }
}