package lk.gamage.backend.healthbridgebackend.service;

import lk.gamage.backend.healthbridgebackend.dto.AuditLogDto;
import lk.gamage.backend.healthbridgebackend.model.AuditLog;
import lk.gamage.backend.healthbridgebackend.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository repository;

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
        return repository.findAll();
    }

    public List<AuditLog> getLogsByRole(String role) {
        return repository.findByRole(role);
    }

    public List<AuditLog> getLogsByEvent(String event) {
        return repository.findByEvent(event);
    }

    public List<AuditLog> getLogsBySeverity(String severity) {
        return repository.findBySeverity(severity);
    }
}
