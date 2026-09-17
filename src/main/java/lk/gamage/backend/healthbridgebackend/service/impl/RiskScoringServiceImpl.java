package lk.gamage.backend.healthbridgebackend.service.impl;

import lk.gamage.backend.healthbridgebackend.model.RiskScore;
import lk.gamage.backend.healthbridgebackend.model.InsuranceClaim;
import lk.gamage.backend.healthbridgebackend.model.FraudAlert;
import lk.gamage.backend.healthbridgebackend.repository.RiskScoreRepository;
import lk.gamage.backend.healthbridgebackend.repository.InsuranceClaimRepository;
import lk.gamage.backend.healthbridgebackend.repository.FraudAlertRepository;
import lk.gamage.backend.healthbridgebackend.service.RiskScoringService;
import lk.gamage.backend.healthbridgebackend.service.RiskScoreStatistics;
import lk.gamage.backend.healthbridgebackend.service.RiskScoreBreakdown;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RiskScoringServiceImpl implements RiskScoringService {

    @Autowired
    private RiskScoreRepository riskScoreRepository;

    @Autowired
    private InsuranceClaimRepository claimRepository;

    @Autowired
    private FraudAlertRepository fraudAlertRepository;

    // Risk score thresholds
    private static final Double LOW_RISK_THRESHOLD = 30.0;
    private static final Double MEDIUM_RISK_THRESHOLD = 60.0;
    private static final Double HIGH_RISK_THRESHOLD = 80.0;

    @Override
    public RiskScore calculateClaimRiskScore(String claimId) {
        try {
            InsuranceClaim claim = claimRepository.findById(claimId)
                    .orElseThrow(() -> new RuntimeException("Claim not found"));

            // Check if there are fraud alerts for this claim
            List<FraudAlert> alerts = fraudAlertRepository.findByClaimId(claimId);

            Double claimRiskScore = 0.0;
            if (!alerts.isEmpty()) {
                claimRiskScore = alerts.stream()
                        .mapToDouble(FraudAlert::getRiskScore)
                        .average()
                        .orElse(0.0);
            }

            return RiskScore.builder()
                    .claimRiskScore(claimRiskScore)
                    .patientId(claim.getPatientId())
                    .doctorId(null)
                    .policyId(claim.getPolicyId())
                    .calculatedAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            System.err.println("Error calculating claim risk score: " + e.getMessage());
            return null;
        }
    }

    @Override
    public RiskScore calculatePatientRiskScore(String patientId) {
        try {
            // Get patient's claim history
            List<InsuranceClaim> claims = claimRepository.findByPatientId(patientId);
            List<FraudAlert> alerts = fraudAlertRepository.findByPatientId(patientId);

            if (claims.isEmpty()) {
                return null; // No claims, no risk
            }

            // Calculate individual risk components
            Double claimAmountScore = calculateClaimAmountAnomaly(claims);
            Double frequencyScore = calculateFrequencyScore(claims);
            Double documentationScore = calculateDocumentationScore(claims);
            Double flaggedClaimsScore = calculateFlaggedClaimsScore(alerts, claims);

            // Weighted average (25%, 20%, 25%, 30%)
            Double totalRisk = (claimAmountScore * 0.25) +
                    (frequencyScore * 0.20) +
                    (documentationScore * 0.25) +
                    (flaggedClaimsScore * 0.30);

            // Cap at 100
            totalRisk = Math.min(totalRisk, 100.0);

            // Determine trend
            Optional<RiskScore> previousScore = riskScoreRepository.findByPatientId(patientId);
            String riskTrend = "STABLE";
            if (previousScore.isPresent()) {
                if (totalRisk > previousScore.get().getPatientRiskScore()) {
                    riskTrend = "INCREASING";
                } else if (totalRisk < previousScore.get().getPatientRiskScore()) {
                    riskTrend = "DECREASING";
                }
            }

            // Count flagged claims
            Integer flaggedCount = (int) alerts.stream()
                    .filter(a -> a.getPatientId().equals(patientId))
                    .count();

            // Count confirmed fraud
            Integer confirmedFraudCount = (int) alerts.stream()
                    .filter(a -> a.getPatientId().equals(patientId) && a.getStatus().equals("CONFIRMED_FRAUD"))
                    .count();

            Integer rejectedClaimsLastYear = claimRepository.countByPatientIdAndStatusAndSubmittedAtAfter(
                    patientId,
                    "REJECTED",
                    LocalDateTime.now().minusYears(1)
            );

            return RiskScore.builder()
                    .patientId(patientId)
                    .patientRiskScore(totalRisk)
                    .claimAmountScore(claimAmountScore)
                    .frequencyScore(frequencyScore)
                    .documentationScore(documentationScore)
                    .totalClaimsLastYear(claims.size())
                    .rejectedClaimsLastYear(rejectedClaimsLastYear != null ? rejectedClaimsLastYear : 0)
                    .flaggedClaimsCount(flaggedCount)
                    .confirmedFraudCount(confirmedFraudCount)
                    .riskTrend(riskTrend)
                    .previousRiskScore(previousScore.map(RiskScore::getPatientRiskScore).orElse(null))
                    .isActive(true)
                    .calculatedAt(LocalDateTime.now())
                    .lastUpdated(LocalDateTime.now())
                    .nextCalculationAt(LocalDateTime.now().plusDays(7))
                    .build();

        } catch (Exception e) {
            System.err.println("Error calculating patient risk score: " + e.getMessage());
            return null;
        }
    }

    @Override
    public RiskScore calculateDoctorRiskScore(String doctorId) {
        return null; // Claims currently do not carry doctor IDs.
    }

    @Override
    public RiskScore getPatientRiskScore(String patientId) {
        try {
            Optional<RiskScore> existingScore = riskScoreRepository.findByPatientId(patientId);

            if (existingScore.isPresent()) {
                RiskScore score = existingScore.get();
                // Recalculate if due for recalculation
                if (score.getNextCalculationAt() != null && 
                    LocalDateTime.now().isAfter(score.getNextCalculationAt())) {
                    return calculatePatientRiskScore(patientId);
                }
                return score;
            }

            // Calculate if doesn't exist
            return calculatePatientRiskScore(patientId);

        } catch (Exception e) {
            System.err.println("Error getting patient risk score: " + e.getMessage());
            return null;
        }
    }

    @Override
    public RiskScore getDoctorRiskScore(String doctorId) {
        try {
            Optional<RiskScore> existingScore = riskScoreRepository.findByDoctorId(doctorId);

            if (existingScore.isPresent()) {
                RiskScore score = existingScore.get();
                if (score.getNextCalculationAt() != null && 
                    LocalDateTime.now().isAfter(score.getNextCalculationAt())) {
                    return calculateDoctorRiskScore(doctorId);
                }
                return score;
            }

            return calculateDoctorRiskScore(doctorId);

        } catch (Exception e) {
            System.err.println("Error getting doctor risk score: " + e.getMessage());
            return null;
        }
    }

    @Override
    public RiskScore getPolicyRiskScore(String policyId) {
        try {
            return riskScoreRepository.findByPolicyId(policyId).orElse(null);
        } catch (Exception e) {
            System.err.println("Error getting policy risk score: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void recalculateAllRiskScores() {
        try {
            // Get all active patients and doctors
            List<InsuranceClaim> allClaims = claimRepository.findAll();

            Set<String> patientIds = allClaims.stream()
                    .map(InsuranceClaim::getPatientId)
                    .collect(Collectors.toSet());

                Set<String> doctorIds = Collections.emptySet();

            // Recalculate all patient scores
            patientIds.forEach(patientId -> {
                RiskScore score = calculatePatientRiskScore(patientId);
                if (score != null) {
                    riskScoreRepository.save(score);
                }
            });

            // Recalculate all doctor scores
            doctorIds.forEach(doctorId -> {
                RiskScore score = calculateDoctorRiskScore(doctorId);
                if (score != null) {
                    riskScoreRepository.save(score);
                }
            });

            System.out.println("Recalculated " + patientIds.size() + " patient and " + doctorIds.size() + " doctor risk scores");

        } catch (Exception e) {
            System.err.println("Error recalculating risk scores: " + e.getMessage());
        }
    }

    @Override
    public List<RiskScore> getHighRiskPatients() {
        try {
            return riskScoreRepository.findByPatientRiskScoreGreaterThanAndIsActive(MEDIUM_RISK_THRESHOLD, true);
        } catch (Exception e) {
            System.err.println("Error getting high-risk patients: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<RiskScore> getHighRiskDoctors() {
        try {
            return riskScoreRepository.findByDoctorRiskScoreGreaterThanAndIsActive(MEDIUM_RISK_THRESHOLD, true);
        } catch (Exception e) {
            System.err.println("Error getting high-risk doctors: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<RiskScore> getPatientsWithIncreasingRisk() {
        try {
            return riskScoreRepository.findIncreasingSuspiciousPatients();
        } catch (Exception e) {
            System.err.println("Error getting increasing risk patients: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<RiskScore> getSuspiciousDoctors() {
        try {
            return riskScoreRepository.findSuspiciousDoctors();
        } catch (Exception e) {
            System.err.println("Error getting suspicious doctors: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public RiskScoreStatistics getRiskScoreStatistics() {
        try {
            Long highRiskPatients = riskScoreRepository.countByPatientRiskScoreGreaterThan(MEDIUM_RISK_THRESHOLD);
            Long highRiskDoctors = riskScoreRepository.countByDoctorRiskScoreGreaterThan(MEDIUM_RISK_THRESHOLD);
            Long totalScores = riskScoreRepository.countByIsActive(true);
            Long increasingTrend = (long) riskScoreRepository.findIncreasingSuspiciousPatients().size();

            return new RiskScoreStatistics(highRiskPatients, highRiskDoctors, totalScores, increasingTrend);

        } catch (Exception e) {
            System.err.println("Error getting risk score statistics: " + e.getMessage());
            return new RiskScoreStatistics(0L, 0L, 0L, 0L);
        }
    }

    @Override
    public void updateAllRiskTrends() {
        try {
            List<RiskScore> allScores = riskScoreRepository.findByIsActive(true);

            allScores.forEach(score -> {
                if (score.getPreviousRiskScore() != null) {
                    Double current = score.getPatientRiskScore() != null ? score.getPatientRiskScore() : score.getDoctorRiskScore();
                    if (current > score.getPreviousRiskScore()) {
                        score.setRiskTrend("INCREASING");
                    } else if (current < score.getPreviousRiskScore()) {
                        score.setRiskTrend("DECREASING");
                    } else {
                        score.setRiskTrend("STABLE");
                    }
                    riskScoreRepository.save(score);
                }
            });

            System.out.println("Updated risk trends for " + allScores.size() + " scores");

        } catch (Exception e) {
            System.err.println("Error updating risk trends: " + e.getMessage());
        }
    }

    @Override
    public RiskScoreBreakdown getPatientRiskBreakdown(String patientId) {
        try {
            RiskScore score = getPatientRiskScore(patientId);
            if (score == null) {
                return null;
            }

            return new RiskScoreBreakdown(
                    score.getPatientRiskScore(),
                    score.getClaimAmountScore(),
                    score.getFrequencyScore(),
                    score.getDocumentationScore(),
                    score.getFlaggedClaimsCount(),
                    score.getRiskTrend()
            );

        } catch (Exception e) {
            System.err.println("Error getting patient risk breakdown: " + e.getMessage());
            return null;
        }
    }

    @Override
    public RiskScoreBreakdown getDoctorRiskBreakdown(String doctorId) {
        try {
            RiskScore score = getDoctorRiskScore(doctorId);
            if (score == null) {
                return null;
            }

            return new RiskScoreBreakdown(
                    score.getDoctorRiskScore(),
                    null,
                    null,
                    null,
                    score.getFlaggedClaimsCount(),
                    score.getRiskTrend()
            );

        } catch (Exception e) {
            System.err.println("Error getting doctor risk breakdown: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void archiveInactiveScores() {
        try {
            List<RiskScore> inactiveScores = riskScoreRepository.findByIsActive(false);

            inactiveScores.forEach(score -> {
                riskScoreRepository.delete(score);
            });

            System.out.println("Archived " + inactiveScores.size() + " inactive scores");

        } catch (Exception e) {
            System.err.println("Error archiving inactive scores: " + e.getMessage());
        }
    }

    // Helper methods
    private Double calculateClaimAmountAnomaly(List<InsuranceClaim> claims) {
        if (claims.isEmpty()) return 0.0;

        Double mean = claims.stream().mapToDouble(InsuranceClaim::getClaimAmount).average().orElse(0.0);
        Double stdDev = calculateStdDeviation(
                claims.stream().map(InsuranceClaim::getClaimAmount).collect(Collectors.toList()),
                mean
        );

        Long outliers = claims.stream()
                .filter(c -> Math.abs(c.getClaimAmount() - mean) > 2 * stdDev)
                .count();

        return Math.min((outliers / (double) claims.size()) * 100, 100.0);
    }

    private Double calculateFrequencyScore(List<InsuranceClaim> claims) {
        if (claims.isEmpty()) return 0.0;

        LocalDateTime lastYear = LocalDateTime.now().minusYears(1);
        Long claimsLastYear = claims.stream()
                .filter(c -> c.getSubmittedAt() != null && c.getSubmittedAt().isAfter(lastYear))
                .count();

        Double averageMonthlyFrequency = claimsLastYear / 12.0;
        Double score = Math.min(averageMonthlyFrequency * 10, 100.0);

        return score;
    }

    private Double calculateDocumentationScore(List<InsuranceClaim> claims) {
        Long claimsWithoutDocs = claims.stream()
                .filter(c -> c.getDocumentFileIds() == null || c.getDocumentFileIds().isEmpty())
                .count();

        return Math.min((claimsWithoutDocs / (double) claims.size()) * 100, 100.0);
    }

    private Double calculateFlaggedClaimsScore(List<FraudAlert> alerts, List<InsuranceClaim> claims) {
        if (claims.isEmpty()) return 0.0;

        Double flaggedPercentage = (alerts.size() / (double) claims.size()) * 100;
        return Math.min(flaggedPercentage, 100.0);
    }

    private Double calculateDoctorClaimAmountAnomaly(List<InsuranceClaim> claims) {
        if (claims.isEmpty()) return 0.0;

        Double mean = claims.stream().mapToDouble(InsuranceClaim::getClaimAmount).average().orElse(0.0);
        Double stdDev = calculateStdDeviation(
                claims.stream().map(InsuranceClaim::getClaimAmount).collect(Collectors.toList()),
                mean
        );

        Long outliers = claims.stream()
                .filter(c -> Math.abs(c.getClaimAmount() - mean) > 2 * stdDev)
                .count();

        return Math.min((outliers / (double) claims.size()) * 50, 100.0);
    }

    private Double calculateStdDeviation(List<Double> values, Double mean) {
        if (values.isEmpty()) return 0.0;

        Double variance = values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average()
                .orElse(0.0);

        return Math.sqrt(variance);
    }
}
