package lk.gamage.backend.healthbridgebackend.service;

import lk.gamage.backend.healthbridgebackend.dto.request.AddContactRequest;
import lk.gamage.backend.healthbridgebackend.dto.request.TriggerSOSRequest;
import lk.gamage.backend.healthbridgebackend.model.EmergencyContact;
import lk.gamage.backend.healthbridgebackend.model.NotificationLog;
import lk.gamage.backend.healthbridgebackend.model.SOSAlert;
import lk.gamage.backend.healthbridgebackend.model.User;
import lk.gamage.backend.healthbridgebackend.repository.ContactRepository;
import lk.gamage.backend.healthbridgebackend.repository.NotificationLogRepository;
import lk.gamage.backend.healthbridgebackend.repository.SOSRepository;
import lk.gamage.backend.healthbridgebackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SOSService {

    private final SOSRepository sosRepository;
    private final UserRepository userRepository;
    private final ContactRepository contactRepository;
    private final NotificationLogRepository notificationLogRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // ========================================================================
    // CORE SOS ALERT LOGIC
    // ========================================================================
    public SOSAlert triggerSOS(String userId, TriggerSOSRequest request) {
        // Mock user if not found (since we skip auth for this prototype)
        User user = userRepository.findById(userId).orElseGet(() -> {
            User mockUser = new User();
            mockUser.setId(userId);
            mockUser.setFirstName("Sarah");
            mockUser.setLastName("Johnson");
            mockUser.setBloodType("O+");
            mockUser.setAllergies(List.of("Penicillin"));
            mockUser.setConditions(List.of("Type 1 Diabetes", "Hypertension"));
            return userRepository.save(mockUser);
        });

        SOSAlert.PatientInfo patientInfo = SOSAlert.PatientInfo.builder()
                .name(user.getFirstName() + " " + user.getLastName())
                .bloodType(user.getBloodType())
                .allergies(user.getAllergies())
                .conditions(user.getConditions())
                .build();

        SOSAlert.Location location = SOSAlert.Location.builder()
                .latitude(request.getLocation().getLatitude())
                .longitude(request.getLocation().getLongitude())
                .address(request.getLocation().getAddress())
                .build();

        SOSAlert alert = SOSAlert.builder()
                .userId(userId)
                .emergencyType(request.getEmergencyType())
                .location(location)
                .patientInfo(patientInfo)
                .status("ACTIVE")
                .triggeredAt(Instant.now())
                .eta("8-12 mins")
                .build();

        alert = sosRepository.save(alert);

        // Notify contacts
        List<EmergencyContact> contacts = contactRepository.findByUserId(userId);
        notifyContacts(alert.getId(), contacts);

        // Broadcast to WebSocket clients (e.g., admin dashboard)
        messagingTemplate.convertAndSend("/topic/alerts", alert);

        return alert;
    }

    public SOSAlert cancelSOS(String alertId) {
        Optional<SOSAlert> optionalAlert = sosRepository.findById(alertId);
        if (optionalAlert.isPresent()) {
            SOSAlert alert = optionalAlert.get();
            if ("ACTIVE".equals(alert.getStatus())) {
                alert.setStatus("CANCELLED");
                alert.setResolvedAt(Instant.now());
                alert = sosRepository.save(alert);
                
                List<EmergencyContact> contacts = contactRepository.findByUserId(alert.getUserId());
                notifyCancellation(alertId, contacts);
                
                messagingTemplate.convertAndSend("/topic/alerts/cancel", alert);
                return alert;
            }
        }
        throw new RuntimeException("Alert not found or already closed");
    }

    public SOSAlert getAlert(String alertId) {
        return sosRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found"));
    }

    public List<SOSAlert> getHistory(String userId) {
        return sosRepository.findByUserId(userId);
    }

    // ========================================================================
    // NOTIFICATION LOGIC
    // ========================================================================
    private void notifyContacts(String alertId, List<EmergencyContact> contacts) {
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

    private void notifyCancellation(String alertId, List<EmergencyContact> contacts) {
        for (EmergencyContact contact : contacts) {
            log.info("Sending Cancellation SMS to {} at {}", contact.getName(), contact.getPhone());
        }
    }

    // ========================================================================
    // EMERGENCY CONTACTS MANAGEMENT
    // ========================================================================
    public List<EmergencyContact> getContacts(String userId) {
        return contactRepository.findByUserId(userId);
    }

    public EmergencyContact addContact(String userId, AddContactRequest request) {
        EmergencyContact contact = EmergencyContact.builder()
                .userId(userId)
                .name(request.getName())
                .relationship(request.getRelationship())
                .phone(request.getPhone())
                .email(request.getEmail())
                .isPrimary(request.isPrimary())
                .createdAt(Instant.now())
                .build();
        return contactRepository.save(contact);
    }

    public EmergencyContact updateContact(String contactId, AddContactRequest request) {
        Optional<EmergencyContact> optContact = contactRepository.findById(contactId);
        if (optContact.isPresent()) {
            EmergencyContact contact = optContact.get();
            contact.setName(request.getName());
            contact.setPhone(request.getPhone());
            contact.setRelationship(request.getRelationship());
            contact.setEmail(request.getEmail());
            contact.setPrimary(request.isPrimary());
            return contactRepository.save(contact);
        }
        throw new RuntimeException("Contact not found");
    }

    public void deleteContact(String contactId) {
        contactRepository.deleteById(contactId);
    }

}