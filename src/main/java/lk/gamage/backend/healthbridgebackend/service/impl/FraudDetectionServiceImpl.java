package lk.gamage.backend.healthbridgebackend.service.impl;

import lk.gamage.backend.healthbridgebackend.enums.FraudAlertType;
import lk.gamage.backend.healthbridgebackend.enums.AlertSeverity;
import lk.gamage.backend.healthbridgebackend.enums.AlertStatus;
import lk.gamage.backend.healthbridgebackend.model.FraudAlert;
import lk.gamage.backend.healthbridgebackend.model.InsuranceClaim;
import lk.gamage.backend.healthbridgebackend.model.MedicalRecord;
import lk.gamage.backend.healthbridgebackend.repository.FraudAlertRepository;
import lk.gamage.backend.healthbridgebackend.repository.InsuranceClaimRepository;
import lk.gamage.backend.healthbridgebackend.repository.MedicalRecordRepository;
import lk.gamage.backend.healthbridgebackend.service.FraudDetectionService;
import lk.gamage.backend.healthbridgebackend.service.NotificationService;
import lk.gamage.backend.healthbridgebackend.service.FraudAlertStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FraudDetectionServiceImpl implements FraudDetectionService {

    @Autowired
    private InsuranceClaimRepository claimRepository;

    @Autowired
    private FraudAlertRepository fraudAlertRepository;

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private NotificationService notificationService;

    // Thresholds for fraud detection
    private static final Double OVERBILLING_THRESHOLD = 1.5;  // 50% above average
    private static final Double HIGH_FREQUENCY_MULTIPLIER = 3.0;  // 3x average frequency
    private static final Integer DUPLICATE_CLAIM_DAYS = 30;  // Within 30 days
    private static final Double DOCTOR_REJECTION_THRESHOLD = 0.30;  // 30% rejection rate

    @Override
    public FraudAlert analyzeClaimForFraud(String claimId) {
        try {
            InsuranceClaim claim = claimRepository.findById(claimId)
                    .orElseThrow(() -> new RuntimeException("Claim not found: " + claimId));

            List<FraudAlert> alerts = new ArrayList<>();

            // Run all fraud detection checks
            FraudAlert duplicateAlert = checkDuplicateClaims(claimId);
            if (duplicateAlert != null) alerts.add(duplicateAlert);

            FraudAlert overbillingAlert = checkOverbilling(claimId);
            if (overbillingAlert != null) alerts.add(overbillingAlert);

            FraudAlert frequencyAlert = checkAbnormalFrequency(claim.getPatientId(), claimId);
            if (frequencyAlert != null) alerts.add(frequencyAlert);

            FraudAlert doctorPatternAlert = checkDoctorPatterns(null);
            if (doctorPatternAlert != null) alerts.add(doctorPatternAlert);

            FraudAlert docMismatchAlert = checkDocumentationMismatch(claimId);
            if (docMismatchAlert != null) alerts.add(docMismatchAlert);

            FraudAlert medicalLogicAlert = checkMedicalLogic(claimId);
            if (medicalLogicAlert != null) alerts.add(medicalLogicAlert);

            // Get highest risk alert
            FraudAlert highestRiskAlert = null;
            if (!alerts.isEmpty()) {
                highestRiskAlert = alerts.stream()
                        .max(Comparator.comparingDouble(FraudAlert::getRiskScore))
                        .orElse(null);

                if (highestRiskAlert != null) {
                    // Link all related alerts
                    highestRiskAlert.setRelatedAlertIds(
                            alerts.stream()
                                    .map(FraudAlert::getId)
                                    .collect(Collectors.toList())
                    );

                    // Save to database
                    highestRiskAlert = fraudAlertRepository.save(highestRiskAlert);

                    // Notify admin if high risk
                    if (highestRiskAlert.getRiskScore() > 70) {
                        notifyAdminFraudAlert(highestRiskAlert);
                    }
                }
            }

            return highestRiskAlert;

        } catch (Exception e) {
            System.err.println("Error analyzing claim for fraud: " + e.getMessage());
            throw new RuntimeException("Fraud detection failed", e);
        }
    }

    @Override
    public FraudAlert checkDuplicateClaims(String claimId) {
        try {
            InsuranceClaim claim = claimRepository.findById(claimId)
                    .orElseThrow(() -> new RuntimeException("Claim not found"));

            // Search for same treatment claimed by same patient within DUPLICATE_CLAIM_DAYS
            List<InsuranceClaim> similarClaims = claimRepository.findByPatientIdAndTreatmentDescriptionAndSubmittedAtAfter(
                    claim.getPatientId(),
                    claim.getTreatmentDescription(),
                    claim.getSubmittedAt().minusDays(DUPLICATE_CLAIM_DAYS)
            );

            if (similarClaims.size() > 1) {
                InsuranceClaim similarClaim = similarClaims.stream()
                        .filter(c -> !c.getId().equals(claimId))
                        .findFirst()
                        .orElse(null);

                return FraudAlert.builder()
                        .claimId(claimId)
                        .patientId(claim.getPatientId())
                        .policyId(claim.getPolicyId())
                        .doctorId(null)
                        .alertType(FraudAlertType.DUPLICATE_CLAIM.toString())
                        .description("Same treatment claimed " + similarClaims.size() + " times within " + DUPLICATE_CLAIM_DAYS + " days")
                        .severity(AlertSeverity.CRITICAL.toString())
                        .riskScore(85.0)
                        .status(AlertStatus.PENDING.toString())
                        .similarClaimId(similarClaim != null ? similarClaim.getId() : null)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
            }

            return null;

        } catch (Exception e) {
            System.err.println("Error in checkDuplicateClaims: " + e.getMessage());
            return null;
        }
    }

    @Override
    public FraudAlert checkOverbilling(String claimId) {
        try {
            InsuranceClaim claim = claimRepository.findById(claimId)
                    .orElseThrow(() -> new RuntimeException("Claim not found"));

            // Get average claim amount for same treatment
                    Double averageAmount = claimRepository.findByTreatmentDescription(
                            claim.getTreatmentDescription())
                        .stream()
                        .map(InsuranceClaim::getClaimAmount)
                        .filter(Objects::nonNull)
                        .mapToDouble(Double::doubleValue)
                        .average()
                        .orElse(0.0);

            if (averageAmount == null || averageAmount == 0) {
                return null; // Not enough data
            }

            Double variance = (claim.getClaimAmount() - averageAmount) / averageAmount;

            if (variance > OVERBILLING_THRESHOLD) {  // 50% higher than average
                return FraudAlert.builder()
                        .claimId(claimId)
                        .patientId(claim.getPatientId())
                        .policyId(claim.getPolicyId())
                        .doctorId(null)
                        .alertType(FraudAlertType.OVERBILLING.toString())
                        .description("Claim amount $" + claim.getClaimAmount() + " is " + String.format("%.0f%%", variance * 100) + " higher than average ($" + averageAmount + ")")
                        .severity(AlertSeverity.HIGH.toString())
                        .riskScore(70.0)
                        .status(AlertStatus.PENDING.toString())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
            }

            return null;

        } catch (Exception e) {
            System.err.println("Error in checkOverbilling: " + e.getMessage());
            return null;
        }
    }

    @Override
    public FraudAlert checkAbnormalFrequency(String patientId, String claimId) {
        try {
            // Count claims last 30 days
            Integer claimsLastMonth = claimRepository.countByPatientIdAndSubmittedAtAfter(
                    patientId,
                    LocalDateTime.now().minusDays(30)
            );

            // Get patient's average monthly frequency
            Integer totalClaimsLastYear = claimRepository.countByPatientIdAndSubmittedAtAfter(
                    patientId,
                    LocalDateTime.now().minusYears(1)
            );

            Double averageMonthlyFrequency = (totalClaimsLastYear != null && totalClaimsLastYear > 0)
                    ? totalClaimsLastYear / 12.0
                    : 2.0; // Default average: 2 claims per month

            if (claimsLastMonth > averageMonthlyFrequency * HIGH_FREQUENCY_MULTIPLIER) {
                return FraudAlert.builder()
                        .claimId(claimId)
                        .patientId(patientId)
                        .alertType(FraudAlertType.HIGH_FREQUENCY.toString())
                        .description("Patient claimed " + claimsLastMonth + " times this month (average: " + String.format("%.1f", averageMonthlyFrequency) + ")")
                        .severity(AlertSeverity.MEDIUM.toString())
                        .riskScore(55.0)
                        .status(AlertStatus.PENDING.toString())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
            }

            return null;

        } catch (Exception e) {
            System.err.println("Error in checkAbnormalFrequency: " + e.getMessage());
            return null;
        }
    }

    @Override
    public FraudAlert checkDoctorPatterns(String doctorId) {
        return null; // Claims currently do not carry doctor IDs.
    }

    @Override
    public FraudAlert checkDocumentationMismatch(String claimId) {
        try {
            InsuranceClaim claim = claimRepository.findById(claimId)
                    .orElseThrow(() -> new RuntimeException("Claim not found"));

            // Check if required documents are present
            if (claim.getDocumentFileIds() == null || claim.getDocumentFileIds().isEmpty()) {
                return FraudAlert.builder()
                        .claimId(claimId)
                        .patientId(claim.getPatientId())
                        .policyId(claim.getPolicyId())
                        .doctorId(null)
                        .alertType(FraudAlertType.DOCUMENTATION_MISMATCH.toString())
                        .description("Missing required supporting documentation")
                        .severity(AlertSeverity.MEDIUM.toString())
                        .riskScore(40.0)
                        .status(AlertStatus.PENDING.toString())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
            }

            return null;

        } catch (Exception e) {
            System.err.println("Error in checkDocumentationMismatch: " + e.getMessage());
            return null;
        }
    }

    @Override
    public FraudAlert checkMedicalLogic(String claimId) {
        try {
            InsuranceClaim claim = claimRepository.findById(claimId)
                    .orElseThrow(() -> new RuntimeException("Claim not found"));

            // Get patient's medical records
            List<MedicalRecord> medicalRecords = medicalRecordRepository.findByPatientIdOrderByVisitDateDesc(claim.getPatientId());

            // Simple logic check: verify treatment matches patient's medical history
            boolean hasMatchingDiagnosis = medicalRecords.stream()
                    .anyMatch(mr -> claim.getTreatmentDescription().toLowerCase()
                            .contains(mr.getDiagnosis().toLowerCase()));

            if (!hasMatchingDiagnosis && !medicalRecords.isEmpty()) {
                return FraudAlert.builder()
                        .claimId(claimId)
                        .patientId(claim.getPatientId())
                        .policyId(claim.getPolicyId())
                        .doctorId(null)
                        .alertType(FraudAlertType.UNUSUAL_DIAGNOSIS.toString())
                        .description("Claimed treatment does not match patient's medical history")
                        .severity(AlertSeverity.MEDIUM.toString())
                        .riskScore(50.0)
                        .status(AlertStatus.PENDING.toString())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
            }

            return null;

        } catch (Exception e) {
            System.err.println("Error in checkMedicalLogic: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<FraudAlert> getPendingAlerts() {
        try {
            return fraudAlertRepository.findByStatusOrderByRiskScoreDesc(AlertStatus.PENDING.toString());
        } catch (Exception e) {
            System.err.println("Error getting pending alerts: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<FraudAlert> getHighRiskAlerts() {
        try {
            return fraudAlertRepository.findByRiskScoreGreaterThan(60.0);
        } catch (Exception e) {
            System.err.println("Error getting high-risk alerts: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<FraudAlert> getRecentAlerts(Integer daysBack) {
        try {
            LocalDateTime startDate = LocalDateTime.now().minusDays(daysBack);
            return fraudAlertRepository.findByCreatedAtAfter(startDate);
        } catch (Exception e) {
            System.err.println("Error getting recent alerts: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<FraudAlert> getAlertsByPatientId(String patientId) {
        try {
            return fraudAlertRepository.findByPatientId(patientId);
        } catch (Exception e) {
            System.err.println("Error getting alerts by patient: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<FraudAlert> getAlertsByClaimId(String claimId) {
        try {
            return fraudAlertRepository.findByClaimId(claimId);
        } catch (Exception e) {
            System.err.println("Error getting alerts by claim: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<FraudAlert> getAlertsByDoctorId(String doctorId) {
        try {
            return fraudAlertRepository.findByDoctorId(doctorId);
        } catch (Exception e) {
            System.err.println("Error getting alerts by doctor: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public FraudAlert reviewAlert(String alertId, String status, String reviewNotes) {
        try {
            FraudAlert alert = fraudAlertRepository.findById(alertId)
                    .orElseThrow(() -> new RuntimeException("Alert not found"));

            alert.setStatus(status);
            alert.setReviewNotes(reviewNotes);
            alert.setReviewedAt(LocalDateTime.now());
            alert.setUpdatedAt(LocalDateTime.now());

            return fraudAlertRepository.save(alert);

        } catch (Exception e) {
            System.err.println("Error reviewing alert: " + e.getMessage());
            throw new RuntimeException("Alert review failed", e);
        }
    }

    @Override
    public FraudAlertStatistics getAlertStatistics() {
        try {
            Long totalAlerts = fraudAlertRepository.count();
            Long pendingAlerts = fraudAlertRepository.countByStatus(AlertStatus.PENDING.toString());
            Long confirmedFraud = fraudAlertRepository.countByStatus(AlertStatus.CONFIRMED_FRAUD.toString());
            Long falsePositives = fraudAlertRepository.countByStatus(AlertStatus.FALSE_POSITIVE.toString());
            Long criticalAlerts = fraudAlertRepository.countBySeverity(AlertSeverity.CRITICAL.toString());

            return new FraudAlertStatistics(
                    totalAlerts,
                    pendingAlerts,
                    confirmedFraud,
                    falsePositives,
                    criticalAlerts
            );

        } catch (Exception e) {
            System.err.println("Error getting alert statistics: " + e.getMessage());
            return new FraudAlertStatistics(0L, 0L, 0L, 0L, 0L);
        }
    }

    @Override
    public void archiveOldAlerts(Integer daysOld) {
        try {
            LocalDateTime archiveDate = LocalDateTime.now().minusDays(daysOld);
            List<FraudAlert> oldAlerts = fraudAlertRepository.findByCreatedAtAfter(archiveDate.minusDays(1))
                    .stream()
                    .filter(a -> a.getCreatedAt().isBefore(archiveDate))
                    .collect(Collectors.toList());

            for (FraudAlert alert : oldAlerts) {
                alert.setStatus(AlertStatus.ARCHIVED.toString());
                fraudAlertRepository.save(alert);
            }

            System.out.println("Archived " + oldAlerts.size() + " old alerts");

        } catch (Exception e) {
            System.err.println("Error archiving old alerts: " + e.getMessage());
        }
    }

    @Override
    public List<FraudAlert> analyzeBulkClaims(List<String> claimIds) {
        try {
            return claimIds.stream()
                    .map(this::analyzeClaimForFraud)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("Error in bulk claim analysis: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private void notifyAdminFraudAlert(FraudAlert alert) {
        try {
            // Send notification to admin
                System.out.println("High-risk fraud alert detected: " + alert.getClaimId());
        } catch (Exception e) {
            System.err.println("Error notifying admin: " + e.getMessage());
        }
    }
}
