package lk.gamage.backend.healthbridgebackend.service;

import lk.gamage.backend.healthbridgebackend.dto.PaymentRequest;
import lk.gamage.backend.healthbridgebackend.model.Payment;
import lk.gamage.backend.healthbridgebackend.model.User;
import lk.gamage.backend.healthbridgebackend.repository.PaymentRepository;
import lk.gamage.backend.healthbridgebackend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Step 1: Patient submits payment with credit card info.
     * - Validates patient exists
     * - Masks card number (only last 4 digits stored)
     * - Generates 6-digit confirmation code
     * - Saves payment as PENDING_CONFIRMATION
     * - Sends confirmation code to patient's registered email
     */
    public Payment initiatePayment(PaymentRequest request) {
        // Find the patient to get their registered email
        User patient = userRepository.findById(request.getPatientId())
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        // Validate card number format (basic check - digits only, 13-19 digits)
        String cleanCardNumber = request.getCardNumber().replaceAll("[\\s-]", "");
        if (!cleanCardNumber.matches("\\d{13,19}")) {
            throw new IllegalArgumentException("Invalid card number format");
        }

        // Validate expiry date format (MM/YY)
        if (!request.getExpiryDate().matches("(0[1-9]|1[0-2])/\\d{2}")) {
            throw new IllegalArgumentException("Invalid expiry date format. Use MM/YY");
        }

        // Validate CVV (3 or 4 digits)
        if (!request.getCvv().matches("\\d{3,4}")) {
            throw new IllegalArgumentException("Invalid CVV format");
        }

        // Mask card number - only store last 4 digits
        String lastFour = cleanCardNumber.substring(cleanCardNumber.length() - 4);
        String maskedCard = "****-****-****-" + lastFour;

        // Generate 6-digit confirmation code
        int code = 100000 + secureRandom.nextInt(900000);
        String confirmationCode = String.valueOf(code);

        // Create payment record
        Payment payment = Payment.builder()
                .patientId(patient.getId())
                .patientEmail(patient.getEmail())
                .patientName(patient.getFullName())
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
        log.info("[Payment] Initiated payment {} for patient {} ({}), amount: {}", 
                savedPayment.getId(), patient.getFullName(), patient.getEmail(), request.getAmount());

        // Send confirmation code to patient's registered email
        emailService.sendPaymentConfirmationEmail(
                patient.getEmail(),
                confirmationCode,
                request.getAmount().toPlainString(),
                request.getDescription()
        );

        return savedPayment;
    }

    /**
     * Step 2: Patient enters the 6-digit code received via email.
     * - Validates the code matches and hasn't expired
     * - Marks payment as CONFIRMED
     */
    public Payment confirmPayment(String paymentId, String confirmationCode) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        // Check if already confirmed
        if ("CONFIRMED".equals(payment.getStatus())) {
            throw new IllegalArgumentException("This payment has already been confirmed");
        }

        // Check if expired
        if ("EXPIRED".equals(payment.getStatus()) || 
            payment.getCodeExpiresAt().isBefore(LocalDateTime.now())) {
            payment.setStatus("EXPIRED");
            paymentRepository.save(payment);
            throw new IllegalArgumentException("Confirmation code has expired. Please initiate a new payment.");
        }

        // Verify confirmation code
        if (!payment.getConfirmationCode().equals(confirmationCode.trim())) {
            throw new IllegalArgumentException("Invalid confirmation code");
        }

        // Confirm payment
        payment.setStatus("CONFIRMED");
        payment.setConfirmedAt(LocalDateTime.now());
        payment.setConfirmationCode(null); // Clear the code after use

        Payment confirmedPayment = paymentRepository.save(payment);
        log.info("[Payment] Payment {} CONFIRMED for patient {}", paymentId, payment.getPatientEmail());

        return confirmedPayment;
    }

    /**
     * Get all payments for a patient, ordered by most recent first.
     */
    public List<Payment> getPaymentsByPatient(String patientId) {
        return paymentRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
    }

    /**
     * Get a single payment by ID.
     */
    public Payment getPaymentById(String paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
    }
}
