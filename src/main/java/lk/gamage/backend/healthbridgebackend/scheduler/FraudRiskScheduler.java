package lk.gamage.backend.healthbridgebackend.scheduler;

import lk.gamage.backend.healthbridgebackend.service.FraudDetectionService;
import lk.gamage.backend.healthbridgebackend.service.RiskScoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class FraudRiskScheduler {

    @Autowired
    private RiskScoringService riskScoringService;

    @Autowired
    private FraudDetectionService fraudDetectionService;

    /**
     * Recalculate all risk scores daily at 2:00 AM
     * Helps keep risk assessments up-to-date
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void recalculateAllRiskScores() {
        try {
            LocalDateTime startTime = LocalDateTime.now();
            System.out.println("[SCHEDULER] Started risk score recalculation at " + startTime);
            
            riskScoringService.recalculateAllRiskScores();
            
            LocalDateTime endTime = LocalDateTime.now();
            long durationSeconds = java.time.temporal.ChronoUnit.SECONDS.between(startTime, endTime);
            System.out.println("[SCHEDULER] Completed risk score recalculation at " + endTime + 
                    " (Duration: " + durationSeconds + "s)");
            
        } catch (Exception e) {
            System.err.println("[SCHEDULER ERROR] Error during risk score recalculation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Update risk trends daily at 3:00 AM
     * Analyzes if risk is increasing, decreasing, or stable
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void updateAllRiskTrends() {
        try {
            System.out.println("[SCHEDULER] Started risk trend update at " + LocalDateTime.now());
            
            riskScoringService.updateAllRiskTrends();
            
            System.out.println("[SCHEDULER] Completed risk trend update at " + LocalDateTime.now());
            
        } catch (Exception e) {
            System.err.println("[SCHEDULER ERROR] Error updating risk trends: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Archive old resolved alerts daily at 4:00 AM
     * Cleans up alerts older than 90 days with RESOLVED or FALSE_POSITIVE status
     */
    @Scheduled(cron = "0 0 4 * * *")
    public void archiveOldAlerts() {
        try {
            System.out.println("[SCHEDULER] Started old alerts archival at " + LocalDateTime.now());
            
            // Archive alerts resolved more than 90 days ago
            fraudDetectionService.archiveOldAlerts(90);
            
            System.out.println("[SCHEDULER] Completed old alerts archival at " + LocalDateTime.now());
            
        } catch (Exception e) {
            System.err.println("[SCHEDULER ERROR] Error archiving old alerts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Archive inactive risk scores daily at 5:00 AM
     * Removes scores for inactive patients/doctors
     */
    @Scheduled(cron = "0 0 5 * * *")
    public void archiveInactiveScores() {
        try {
            System.out.println("[SCHEDULER] Started inactive scores archival at " + LocalDateTime.now());
            
            riskScoringService.archiveInactiveScores();
            
            System.out.println("[SCHEDULER] Completed inactive scores archival at " + LocalDateTime.now());
            
        } catch (Exception e) {
            System.err.println("[SCHEDULER ERROR] Error archiving inactive scores: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Perform periodic cleanup and maintenance every week (Sunday at 6:00 AM)
     */
    @Scheduled(cron = "0 0 6 ? * SUN")
    public void weeklyMaintenance() {
        try {
            System.out.println("[SCHEDULER] Started weekly maintenance at " + LocalDateTime.now());
            
            // Run all maintenance tasks
            recalculateAllRiskScores();
            updateAllRiskTrends();
            archiveOldAlerts();
            archiveInactiveScores();
            
            System.out.println("[SCHEDULER] Completed weekly maintenance at " + LocalDateTime.now());
            
        } catch (Exception e) {
            System.err.println("[SCHEDULER ERROR] Error during weekly maintenance: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
