package lk.gamage.backend.healthbridgebackend.controller;

import lk.gamage.backend.healthbridgebackend.dto.request.AlertReviewRequest;
import lk.gamage.backend.healthbridgebackend.dto.request.FraudAlertRequest;
import lk.gamage.backend.healthbridgebackend.dto.response.FraudAlertResponse;
import lk.gamage.backend.healthbridgebackend.dto.response.FraudAlertStatisticsResponse;
import lk.gamage.backend.healthbridgebackend.mapper.FraudMapper;
import lk.gamage.backend.healthbridgebackend.model.FraudAlert;
import lk.gamage.backend.healthbridgebackend.service.FraudDetectionService;
import lk.gamage.backend.healthbridgebackend.service.FraudAlertStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/fraud/alerts")
@CrossOrigin(origins = "http://localhost:3000")
public class FraudAlertController {

    @Autowired
    private FraudDetectionService fraudDetectionService;

    @Autowired
    private FraudMapper fraudMapper;

    /**
     * Get all pending fraud alerts
     * GET /api/fraud/alerts/pending
     */
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingAlerts() {
        try {
            List<FraudAlert> alerts = fraudDetectionService.getPendingAlerts();
            List<FraudAlertResponse> responses = alerts.stream()
                    .map(fraudMapper::toFraudAlertResponse)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(Map.of(
                    "message", "Pending alerts retrieved successfully",
                    "count", responses.size(),
                    "data", responses
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error retrieving pending alerts: " + e.getMessage()));
        }
    }

    /**
     * Get high-risk fraud alerts
     * GET /api/fraud/alerts/high-risk
     */
    @GetMapping("/high-risk")
    public ResponseEntity<?> getHighRiskAlerts() {
        try {
            List<FraudAlert> alerts = fraudDetectionService.getHighRiskAlerts();
            List<FraudAlertResponse> responses = alerts.stream()
                    .map(fraudMapper::toFraudAlertResponse)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(Map.of(
                    "message", "High-risk alerts retrieved successfully",
                    "count", responses.size(),
                    "data", responses
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error retrieving high-risk alerts: " + e.getMessage()));
        }
    }

    /**
     * Get recent alerts from last N days
     * GET /api/fraud/alerts/recent?days=7
     */
    @GetMapping("/recent")
    public ResponseEntity<?> getRecentAlerts(@RequestParam(defaultValue = "7") Integer days) {
        try {
            List<FraudAlert> alerts = fraudDetectionService.getRecentAlerts(days);
            List<FraudAlertResponse> responses = alerts.stream()
                    .map(fraudMapper::toFraudAlertResponse)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(Map.of(
                    "message", "Recent alerts retrieved successfully",
                    "days", days,
                    "count", responses.size(),
                    "data", responses
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error retrieving recent alerts: " + e.getMessage()));
        }
    }

    /**
     * Get alerts for a specific patient
     * GET /api/fraud/alerts/patient/{patientId}
     */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<?> getAlertsByPatientId(@PathVariable String patientId) {
        try {
            List<FraudAlert> alerts = fraudDetectionService.getAlertsByPatientId(patientId);
            List<FraudAlertResponse> responses = alerts.stream()
                    .map(fraudMapper::toFraudAlertResponse)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(Map.of(
                    "message", "Patient alerts retrieved successfully",
                    "patientId", patientId,
                    "count", responses.size(),
                    "data", responses
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error retrieving patient alerts: " + e.getMessage()));
        }
    }

    /**
     * Get alerts for a specific claim
     * GET /api/fraud/alerts/claim/{claimId}
     */
    @GetMapping("/claim/{claimId}")
    public ResponseEntity<?> getAlertsByClaimId(@PathVariable String claimId) {
        try {
            List<FraudAlert> alerts = fraudDetectionService.getAlertsByClaimId(claimId);
            List<FraudAlertResponse> responses = alerts.stream()
                    .map(fraudMapper::toFraudAlertResponse)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(Map.of(
                    "message", "Claim alerts retrieved successfully",
                    "claimId", claimId,
                    "count", responses.size(),
                    "data", responses
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error retrieving claim alerts: " + e.getMessage()));
        }
    }

    /**
     * Get alerts for a specific doctor
     * GET /api/fraud/alerts/doctor/{doctorId}
     */
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<?> getAlertsByDoctorId(@PathVariable String doctorId) {
        try {
            List<FraudAlert> alerts = fraudDetectionService.getAlertsByDoctorId(doctorId);
            List<FraudAlertResponse> responses = alerts.stream()
                    .map(fraudMapper::toFraudAlertResponse)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(Map.of(
                    "message", "Doctor alerts retrieved successfully",
                    "doctorId", doctorId,
                    "count", responses.size(),
                    "data", responses
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error retrieving doctor alerts: " + e.getMessage()));
        }
    }

    /**
     * Analyze a specific claim for fraud
     * POST /api/fraud/alerts/analyze/{claimId}
     */
    @PostMapping("/analyze/{claimId}")
    public ResponseEntity<?> analyzeClaim(@PathVariable String claimId) {
        try {
            FraudAlert alert = fraudDetectionService.analyzeClaimForFraud(claimId);
            
            return ResponseEntity.ok(Map.of(
                    "message", alert != null ? "Fraud detected" : "No fraud detected",
                    "claimId", claimId,
                    "alert", alert != null ? fraudMapper.toFraudAlertResponse(alert) : null
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error analyzing claim: " + e.getMessage()));
        }
    }

    /**
     * Review an alert and mark as confirmed or false positive
     * PUT /api/fraud/alerts/{alertId}/review
     */
    @PutMapping("/{alertId}/review")
    public ResponseEntity<?> reviewAlert(
            @PathVariable String alertId,
            @Valid @RequestBody AlertReviewRequest request,
            BindingResult bindingResult) {
        
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(err -> 
                    errors.put(err.getField(), err.getDefaultMessage()));
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Validation failed", "errors", errors));
        }

        try {
            FraudAlert reviewedAlert = fraudDetectionService.reviewAlert(
                    alertId,
                    request.getStatus(),
                    request.getReviewNotes()
            );
            
            return ResponseEntity.ok(Map.of(
                    "message", "Alert reviewed successfully",
                    "data", fraudMapper.toFraudAlertResponse(reviewedAlert)
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error reviewing alert: " + e.getMessage()));
        }
    }

    /**
     * Get fraud alert statistics
     * GET /api/fraud/alerts/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getAlertStatistics() {
        try {
            FraudAlertStatistics statistics = fraudDetectionService.getAlertStatistics();
            
            return ResponseEntity.ok(Map.of(
                    "message", "Alert statistics retrieved successfully",
                    "data", fraudMapper.toFraudAlertStatisticsResponse(statistics)
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error retrieving statistics: " + e.getMessage()));
        }
    }

    /**
     * Trigger bulk analysis of multiple claims
     * POST /api/fraud/alerts/bulk-analyze
     */
    @PostMapping("/bulk-analyze")
    public ResponseEntity<?> bulkAnalyzeClaims(@RequestBody List<String> claimIds) {
        try {
            if (claimIds == null || claimIds.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Claim IDs list cannot be empty"));
            }

            List<FraudAlert> alerts = fraudDetectionService.analyzeBulkClaims(claimIds);
            List<FraudAlertResponse> responses = alerts.stream()
                    .map(fraudMapper::toFraudAlertResponse)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(Map.of(
                    "message", "Bulk analysis completed successfully",
                    "processedCount", claimIds.size(),
                    "fraudDetectedCount", responses.size(),
                    "data", responses
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error in bulk analysis: " + e.getMessage()));
        }
    }
}
