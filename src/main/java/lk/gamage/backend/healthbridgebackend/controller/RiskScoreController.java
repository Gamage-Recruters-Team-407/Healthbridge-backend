package lk.gamage.backend.healthbridgebackend.controller;

import lk.gamage.backend.healthbridgebackend.dto.response.RiskScoreBreakdownResponse;
import lk.gamage.backend.healthbridgebackend.dto.response.RiskScoreResponse;
import lk.gamage.backend.healthbridgebackend.dto.response.RiskScoreStatisticsResponse;
import lk.gamage.backend.healthbridgebackend.mapper.FraudMapper;
import lk.gamage.backend.healthbridgebackend.model.RiskScore;
import lk.gamage.backend.healthbridgebackend.service.RiskScoringService;
import lk.gamage.backend.healthbridgebackend.service.RiskScoreBreakdown;
import lk.gamage.backend.healthbridgebackend.service.RiskScoreStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/fraud/risk-scores")
@CrossOrigin(origins = "http://localhost:3000")
public class RiskScoreController {

    @Autowired
    private RiskScoringService riskScoringService;

    @Autowired
    private FraudMapper fraudMapper;

    /**
     * Get risk score for a specific patient
     * GET /api/fraud/risk-scores/patient/{patientId}
     */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<?> getPatientRiskScore(@PathVariable String patientId) {
        try {
            RiskScore score = riskScoringService.getPatientRiskScore(patientId);
            
            if (score == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(Map.of(
                    "message", "Patient risk score retrieved successfully",
                    "patientId", patientId,
                    "data", fraudMapper.toRiskScoreResponse(score)
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error retrieving patient risk score: " + e.getMessage()));
        }
    }

    /**
     * Get risk score for a specific doctor
     * GET /api/fraud/risk-scores/doctor/{doctorId}
     */
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<?> getDoctorRiskScore(@PathVariable String doctorId) {
        try {
            RiskScore score = riskScoringService.getDoctorRiskScore(doctorId);
            
            if (score == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(Map.of(
                    "message", "Doctor risk score retrieved successfully",
                    "doctorId", doctorId,
                    "data", fraudMapper.toRiskScoreResponse(score)
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error retrieving doctor risk score: " + e.getMessage()));
        }
    }

    /**
     * Get risk score for a specific claim
     * GET /api/fraud/risk-scores/claim/{claimId}
     */
    @GetMapping("/claim/{claimId}")
    public ResponseEntity<?> getClaimRiskScore(@PathVariable String claimId) {
        try {
            RiskScore score = riskScoringService.calculateClaimRiskScore(claimId);
            
            if (score == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(Map.of(
                    "message", "Claim risk score calculated successfully",
                    "claimId", claimId,
                    "data", fraudMapper.toRiskScoreResponse(score)
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error calculating claim risk score: " + e.getMessage()));
        }
    }

    /**
     * Get all high-risk patients
     * GET /api/fraud/risk-scores/high-risk/patients
     */
    @GetMapping("/high-risk/patients")
    public ResponseEntity<?> getHighRiskPatients() {
        try {
            List<RiskScore> scores = riskScoringService.getHighRiskPatients();
            List<RiskScoreResponse> responses = scores.stream()
                    .map(fraudMapper::toRiskScoreResponse)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(Map.of(
                    "message", "High-risk patients retrieved successfully",
                    "count", responses.size(),
                    "data", responses
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error retrieving high-risk patients: " + e.getMessage()));
        }
    }

    /**
     * Get all high-risk doctors
     * GET /api/fraud/risk-scores/high-risk/doctors
     */
    @GetMapping("/high-risk/doctors")
    public ResponseEntity<?> getHighRiskDoctors() {
        try {
            List<RiskScore> scores = riskScoringService.getHighRiskDoctors();
            List<RiskScoreResponse> responses = scores.stream()
                    .map(fraudMapper::toRiskScoreResponse)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(Map.of(
                    "message", "High-risk doctors retrieved successfully",
                    "count", responses.size(),
                    "data", responses
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error retrieving high-risk doctors: " + e.getMessage()));
        }
    }

    /**
     * Get patients with increasing risk trend
     * GET /api/fraud/risk-scores/increasing-risk/patients
     */
    @GetMapping("/increasing-risk/patients")
    public ResponseEntity<?> getPatientsWithIncreasingRisk() {
        try {
            List<RiskScore> scores = riskScoringService.getPatientsWithIncreasingRisk();
            List<RiskScoreResponse> responses = scores.stream()
                    .map(fraudMapper::toRiskScoreResponse)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(Map.of(
                    "message", "Patients with increasing risk retrieved successfully",
                    "count", responses.size(),
                    "data", responses
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error retrieving increasing risk patients: " + e.getMessage()));
        }
    }

    /**
     * Get suspicious doctors
     * GET /api/fraud/risk-scores/suspicious/doctors
     */
    @GetMapping("/suspicious/doctors")
    public ResponseEntity<?> getSuspiciousDoctors() {
        try {
            List<RiskScore> scores = riskScoringService.getSuspiciousDoctors();
            List<RiskScoreResponse> responses = scores.stream()
                    .map(fraudMapper::toRiskScoreResponse)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(Map.of(
                    "message", "Suspicious doctors retrieved successfully",
                    "count", responses.size(),
                    "data", responses
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error retrieving suspicious doctors: " + e.getMessage()));
        }
    }

    /**
     * Get risk score breakdown for a patient
     * GET /api/fraud/risk-scores/patient/{patientId}/breakdown
     */
    @GetMapping("/patient/{patientId}/breakdown")
    public ResponseEntity<?> getPatientRiskBreakdown(@PathVariable String patientId) {
        try {
            RiskScoreBreakdown breakdown = riskScoringService.getPatientRiskBreakdown(patientId);
            
            if (breakdown == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(Map.of(
                    "message", "Patient risk breakdown retrieved successfully",
                    "patientId", patientId,
                    "data", fraudMapper.toRiskScoreBreakdownResponse(breakdown)
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error retrieving risk breakdown: " + e.getMessage()));
        }
    }

    /**
     * Get risk score breakdown for a doctor
     * GET /api/fraud/risk-scores/doctor/{doctorId}/breakdown
     */
    @GetMapping("/doctor/{doctorId}/breakdown")
    public ResponseEntity<?> getDoctorRiskBreakdown(@PathVariable String doctorId) {
        try {
            RiskScoreBreakdown breakdown = riskScoringService.getDoctorRiskBreakdown(doctorId);
            
            if (breakdown == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(Map.of(
                    "message", "Doctor risk breakdown retrieved successfully",
                    "doctorId", doctorId,
                    "data", fraudMapper.toRiskScoreBreakdownResponse(breakdown)
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error retrieving risk breakdown: " + e.getMessage()));
        }
    }

    /**
     * Get risk score statistics
     * GET /api/fraud/risk-scores/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getRiskScoreStatistics() {
        try {
            RiskScoreStatistics statistics = riskScoringService.getRiskScoreStatistics();
            
            return ResponseEntity.ok(Map.of(
                    "message", "Risk score statistics retrieved successfully",
                    "data", fraudMapper.toRiskScoreStatisticsResponse(statistics)
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error retrieving statistics: " + e.getMessage()));
        }
    }

    /**
     * Trigger manual recalculation of all risk scores
     * POST /api/fraud/risk-scores/recalculate
     */
    @PostMapping("/recalculate")
    public ResponseEntity<?> recalculateAllRiskScores() {
        try {
            riskScoringService.recalculateAllRiskScores();
            
            return ResponseEntity.ok(Map.of(
                    "message", "All risk scores recalculated successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error recalculating risk scores: " + e.getMessage()));
        }
    }

    /**
     * Update risk trends for all scores
     * POST /api/fraud/risk-scores/update-trends
     */
    @PostMapping("/update-trends")
    public ResponseEntity<?> updateAllRiskTrends() {
        try {
            riskScoringService.updateAllRiskTrends();
            
            return ResponseEntity.ok(Map.of(
                    "message", "Risk trends updated successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error updating risk trends: " + e.getMessage()));
        }
    }
}
