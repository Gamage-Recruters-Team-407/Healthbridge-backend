package lk.gamage.backend.healthbridgebackend.service;

import lk.gamage.backend.healthbridgebackend.dto.request.TriggerSOSRequest;
import lk.gamage.backend.healthbridgebackend.model.EmergencyContact;
import lk.gamage.backend.healthbridgebackend.model.SOSAlert;
import lk.gamage.backend.healthbridgebackend.model.User;
import lk.gamage.backend.healthbridgebackend.repository.ContactRepository;
import lk.gamage.backend.healthbridgebackend.repository.SOSRepository;
import lk.gamage.backend.healthbridgebackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SOSService {

    private final SOSRepository sosRepository;
    private final UserRepository userRepository;
    private final ContactRepository contactRepository;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

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
        notificationService.notifyContacts(alert.getId(), contacts);

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
                notificationService.notifyCancellation(alertId, contacts);
                
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
}
