package lk.gamage.backend.healthbridgebackend.service;

import lk.gamage.backend.healthbridgebackend.model.EmergencyContact;
import lk.gamage.backend.healthbridgebackend.model.NotificationLog;
import lk.gamage.backend.healthbridgebackend.model.LabResult;
import lk.gamage.backend.healthbridgebackend.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationLogRepository notificationLogRepository;

    public void notifyContacts(String alertId, List<EmergencyContact> contacts) {
        for (EmergencyContact contact : contacts) {
            // Mock sending SMS
            log.info("Sending SMS to {} at {}", contact.getName(), contact.getPhone());
            
            NotificationLog nLog = NotificationLog.builder()
                    .alertId(alertId)
                    .contactId(contact.getId())
                    .contactName(contact.getName())
                    .contactPhone(contact.getPhone())
                    .notificationType("SMS")
                    .status("SENT")
                    .sentAt(Instant.now())
                    .build();
                    
            notificationLogRepository.save(nLog);
        }
    }

    public void notifyCancellation(String alertId, List<EmergencyContact> contacts) {
        for (EmergencyContact contact : contacts) {
            log.info("Sending Cancellation SMS to {} at {}", contact.getName(), contact.getPhone());
        }
    }

    public void sendCriticalResultAlert(LabResult saved) {
        // TODO: implement actual notification logic (email/SMS/push)
        System.out.println("CRITICAL ALERT: Result " + saved.getId() + " is critical for patient " + saved.getPatientId());
    }

    public void notifyResultAvailable(LabResult saved) {
        // TODO: implement actual notification logic
        System.out.println("Result published for patient " + saved.getPatientId());
    }
}
