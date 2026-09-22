package lk.gamage.backend.healthbridgebackend.service;

import lk.gamage.backend.healthbridgebackend.model.RiskScore;

import java.util.List;

public interface RiskScoringService {

    /**
     * Calculate overall risk score for a specific claim
     */
    RiskScore calculateClaimRiskScore(String claimId);

    /**
     * Calculate overall risk score for a patient based on all their claims
     */
    RiskScore calculatePatientRiskScore(String patientId);

    /**
     * Calculate overall risk score for a doctor based on all their submissions
     */
    RiskScore calculateDoctorRiskScore(String doctorId);

    /**
     * Get existing patient risk score
     */
    RiskScore getPatientRiskScore(String patientId);

    /**
     * Get existing doctor risk score
     */
    RiskScore getDoctorRiskScore(String doctorId);

    /**
     * Get existing policy risk score
     */
    RiskScore getPolicyRiskScore(String policyId);

    /**
     * Recalculate all risk scores in the system
     * Should be run periodically (e.g., daily)
     */
    void recalculateAllRiskScores();

    /**
     * Get high-risk patients (risk score > 50)
     */
    List<RiskScore> getHighRiskPatients();

    /**
     * Get high-risk doctors (risk score > 60)
     */
    List<RiskScore> getHighRiskDoctors();

    /**
     * Get patients with increasing risk trend
     */
    List<RiskScore> getPatientsWithIncreasingRisk();

    /**
     * Get doctors with suspicious patterns
     */
    List<RiskScore> getSuspiciousDoctors();

    /**
     * Get risk score statistics
     */
    RiskScoreStatistics getRiskScoreStatistics();

    /**
     * Update risk trend for all entities
     */
    void updateAllRiskTrends();

    /**
     * Get risk score breakdown for a patient
     */
    RiskScoreBreakdown getPatientRiskBreakdown(String patientId);

    /**
     * Get risk score breakdown for a doctor
     */
    RiskScoreBreakdown getDoctorRiskBreakdown(String doctorId);

    /**
     * Archive inactive risk scores
     */
    void archiveInactiveScores();
}
