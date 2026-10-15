package lk.gamage.backend.healthbridgebackend.service;

import jakarta.annotation.PostConstruct;
import lk.gamage.backend.healthbridgebackend.dto.AuditLogDto;
import lk.gamage.backend.healthbridgebackend.model.AuditLog;
import lk.gamage.backend.healthbridgebackend.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.io.ByteArrayOutputStream;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository repository;
    private final MongoTemplate mongoTemplate;

    @PostConstruct
    public void seedInitialLogs() {
        if (repository.count() == 0) {
            LocalDateTime now = LocalDateTime.now();
            List<AuditLog> initialLogs = List.of(
                createLog("Super Admin", "SUPER_ADMIN", "Login", "Authentication", "Admin session established via 2FA", "SESSION-9841", "192.168.1.10 (Chrome / macOS)", "Success", "Normal", now.minusMinutes(12)),
                createLog("Super Admin", "SUPER_ADMIN", "Role Permissions Updated", "Role Management", "Updated permissions for ROLE_DOCTOR", "ROLE-DOC-01", "192.168.1.10 (Chrome / macOS)", "Success", "Medium", now.minusMinutes(35)),
                createLog("Dr. Kasun Perera", "DOCTOR", "Record Access", "Medical Records", "Accessed patient health record for PR-90412", "PR-90412", "192.168.2.45 (Firefox / Windows)", "Success", "Normal", now.minusHours(1).minusMinutes(15)),
                createLog("System Guard", "SYSTEM", "Brute Force Warning", "Security", "Repeated failed login attempts detected", "IP-203.94.1.8", "203.94.1.8 (Unknown Client)", "Failed", "High", now.minusHours(2)),
                createLog("Colombo Branch Admin", "ADMIN", "Staff Member Onboarded", "Staff Management", "Registered Nurse Anoma Silva to ER Unit", "STF-2026-N08", "192.168.3.12 (Edge / Windows)", "Success", "Normal", now.minusHours(3)),
                createLog("Pharmacist Kamal", "PHARMACIST", "Prescription Dispensed", "Pharmacy", "Dispensed RX-8491 Amoxicillin 500mg", "RX-8491", "192.168.4.19 (Chrome / Linux)", "Success", "Normal", now.minusHours(4)),
                createLog("Super Admin", "SUPER_ADMIN", "Branch Settings Updated", "Branch Management", "Updated Kalutara Branch bed capacity to 75", "BR-KAL-01", "192.168.1.10 (Chrome / macOS)", "Success", "Normal", now.minusHours(5)),
                createLog("Lab Tech Nimal", "LAB_OFFICER", "Lab Report Uploaded", "Laboratory", "Uploaded CBC Blood Panel results", "LAB-RES-4412", "192.168.5.88 (Chrome / Windows)", "Success", "Normal", now.minusHours(6))
            );
            repository.saveAll(initialLogs);
        }
    }

    private AuditLog createLog(String user, String role, String event, String module, String actionDetails, String refId, String ipDevice, String status, String severity, LocalDateTime timestamp) {
        AuditLog log = new AuditLog();
        log.setUser(user);
        log.setRole(role);
        log.setEvent(event);
        log.setModule(module);
        log.setActionDetails(actionDetails);
        log.setRefId(refId);
        log.setIpDevice(ipDevice);
        log.setStatus(status);
        log.setSeverity(severity);
        log.setTimestamp(timestamp);
        return log;
    }

    public AuditLog logAction(AuditLogDto dto) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUser(dto.getUser());
        auditLog.setRole(dto.getRole());
        auditLog.setEvent(dto.getEvent());
        auditLog.setModule(dto.getModule());
        auditLog.setActionDetails(dto.getActionDetails());
        auditLog.setRefId(dto.getRefId());
        auditLog.setIpDevice(dto.getIpDevice());
        auditLog.setStatus(dto.getStatus());
        auditLog.setSeverity(dto.getSeverity());
        auditLog.setTimestamp(LocalDateTime.now());
        
        return repository.save(auditLog);
    }

    public List<AuditLog> getAllLogs() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "timestamp"));
    }

    public List<AuditLog> getLogsByRole(String role) {
        Query q = new Query(Criteria.where("role").is(role)).with(Sort.by(Sort.Direction.DESC, "timestamp"));
        return mongoTemplate.find(q, AuditLog.class);
    }

    public List<AuditLog> getLogsByEvent(String event) {
        Query q = new Query(Criteria.where("event").is(event)).with(Sort.by(Sort.Direction.DESC, "timestamp"));
        return mongoTemplate.find(q, AuditLog.class);
    }

    public List<AuditLog> getLogsBySeverity(String severity) {
        Query q = new Query(Criteria.where("severity").is(severity)).with(Sort.by(Sort.Direction.DESC, "timestamp"));
        return mongoTemplate.find(q, AuditLog.class);
    }

    public java.util.Map<String, Long> getTodaySummary() {
        LocalDateTime startOfDay = LocalDateTime.now().with(java.time.LocalTime.MIN);
        Query todayQuery = new Query(Criteria.where("timestamp").gt(startOfDay));
        long totalToday = mongoTemplate.count(todayQuery, AuditLog.class);

        Criteria baseCrit = totalToday > 0 ? Criteria.where("timestamp").gt(startOfDay) : new Criteria();

        long authEvents = mongoTemplate.count(new Query(new Criteria().andOperator(
                baseCrit,
                new Criteria().orOperator(
                        Criteria.where("module").regex("Auth", "i"),
                        Criteria.where("module").regex("Security", "i")
                )
        )), AuditLog.class);

        long adminActions = mongoTemplate.count(new Query(new Criteria().andOperator(
                baseCrit,
                Criteria.where("role").regex("Admin", "i")
        )), AuditLog.class);

        long recordAccess = mongoTemplate.count(new Query(new Criteria().andOperator(
                baseCrit,
                Criteria.where("event").regex("Access", "i")
        )), AuditLog.class);

        long securityEvents = mongoTemplate.count(new Query(new Criteria().andOperator(
                baseCrit,
                Criteria.where("severity").regex("^High$", "i")
        )), AuditLog.class);

        long failedActions = mongoTemplate.count(new Query(new Criteria().andOperator(
                baseCrit,
                Criteria.where("status").regex("^Failed$", "i")
        )), AuditLog.class);

        long effectiveTotal = totalToday > 0 ? totalToday : mongoTemplate.count(new Query(), AuditLog.class);

        return java.util.Map.of(
            "totalToday", effectiveTotal,
            "authEvents", authEvents,
            "adminActions", adminActions,
            "recordAccess", recordAccess,
            "securityEvents", securityEvents,
            "failedActions", failedActions
        );
    }

    public byte[] generateAuditReport() {
        try {
            List<AuditLog> logs = repository.findAll(Sort.by(Sort.Direction.DESC, "timestamp"));
            Document document = new Document();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, out);
            
            document.open();
            document.add(new Paragraph("HealthBridge - Audit Logs Report"));
            document.add(new Paragraph("Generated on: " + LocalDateTime.now().toString()));
            document.add(new Paragraph("Total Logs: " + logs.size()));
            document.add(new Paragraph("--------------------------------------------------"));
            
            for (int i = 0; i < Math.min(logs.size(), 100); i++) {
                AuditLog log = logs.get(i);
                document.add(new Paragraph(String.format("[%s] %s (%s) - %s: %s [%s]", 
                    log.getTimestamp(), log.getUser(), log.getRole(), log.getEvent(), log.getActionDetails(), log.getSeverity())));
            }
            if (logs.size() > 100) {
                document.add(new Paragraph("... and " + (logs.size() - 100) + " more logs."));
            }
            
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }
}
