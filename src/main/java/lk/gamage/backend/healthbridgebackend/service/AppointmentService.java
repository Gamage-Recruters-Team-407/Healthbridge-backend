package lk.gamage.backend.healthbridgebackend.service;

import lk.gamage.backend.healthbridgebackend.dto.request.AppointmentRequest;
import lk.gamage.backend.healthbridgebackend.model.Appointment;
import java.util.List;

public interface AppointmentService {
    List<Appointment> find(String patientId, String status);
    Appointment findById(String id);
    Appointment create(AppointmentRequest request);
    Appointment reschedule(String id, AppointmentRequest request);
    Appointment cancel(String id, String reason);
}
