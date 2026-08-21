package lk.gamage.backend.healthbridgebackend.service;

import lk.gamage.backend.healthbridgebackend.model.LabResult;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    public void sendCriticalResultAlert(LabResult saved) {
        // TODO: implement actual notification logic (email/SMS/push)
        System.out.println("CRITICAL ALERT: Result " + saved.getId() + " is critical for patient " + saved.getPatientId());
    }

    public void notifyResultAvailable(LabResult saved) {
        // TODO: implement actual notification logic
        System.out.println("Result published for patient " + saved.getPatientId());
    }
}
