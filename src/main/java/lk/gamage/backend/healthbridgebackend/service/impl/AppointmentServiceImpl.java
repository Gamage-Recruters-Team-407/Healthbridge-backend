package lk.gamage.backend.healthbridgebackend.service.impl;

import lk.gamage.backend.healthbridgebackend.dto.request.AppointmentRequest;
import lk.gamage.backend.healthbridgebackend.enums.AppointmentStatus;
import lk.gamage.backend.healthbridgebackend.exception.BadRequestException;
import lk.gamage.backend.healthbridgebackend.exception.ResourceNotFoundException;
import lk.gamage.backend.healthbridgebackend.model.Appointment;
import lk.gamage.backend.healthbridgebackend.repository.AppointmentRepository;
import lk.gamage.backend.healthbridgebackend.service.AppointmentService;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class AppointmentServiceImpl implements AppointmentService {
    private final AppointmentRepository repository;
    public AppointmentServiceImpl(AppointmentRepository repository) { this.repository = repository; }
    public List<Appointment> find(String patientId, String status) {
        return repository.findAll().stream().filter(a -> patientId == null || patientId.isBlank() || patientId.equals(a.getPatientId()))
            .filter(a -> status == null || status.isBlank() || "ALL".equalsIgnoreCase(status) || a.getStatus().name().equalsIgnoreCase(status))
            .sorted(Comparator.comparing(Appointment::getAppointmentDate).thenComparing(Appointment::getAppointmentTime)).toList();
    }
    public Appointment findById(String id) { return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Appointment not found: " + id)); }
    public Appointment create(AppointmentRequest r) { validate(r, null); Appointment a = new Appointment("APT-" + UUID.randomUUID(), r.patientId(), r.doctorId(), r.doctorName(), r.doctorSpecialization(), r.hospital(), r.appointmentDate(), r.appointmentTime(), r.appointmentType(), r.reason()); return repository.save(a); }
    public Appointment reschedule(String id, AppointmentRequest r) { Appointment a = findById(id); if (a.getStatus() != AppointmentStatus.UPCOMING) throw new BadRequestException("Only upcoming appointments can be rescheduled."); validate(r, id); a.setAppointmentDate(r.appointmentDate()); a.setAppointmentTime(r.appointmentTime()); a.setReason(r.reason()); a.setUpdatedAt(LocalDateTime.now()); return repository.save(a); }
    public Appointment cancel(String id, String reason) { Appointment a = findById(id); if (a.getStatus() != AppointmentStatus.UPCOMING) throw new BadRequestException("Only upcoming appointments can be cancelled."); a.setStatus(AppointmentStatus.CANCELLED); a.setCancellationReason(reason == null || reason.isBlank() ? "Cancelled by patient." : reason.trim()); a.setUpdatedAt(LocalDateTime.now()); return repository.save(a); }
    private void validate(AppointmentRequest r, String ignoredId) { if (r == null || r.patientId() == null || r.patientId().isBlank() || r.doctorId() == null || r.doctorId().isBlank() || r.appointmentDate() == null || r.appointmentTime() == null || r.appointmentTime().isBlank()) throw new BadRequestException("Patient, doctor, date and time are required."); if (r.appointmentDate().isBefore(LocalDate.now())) throw new BadRequestException("Appointment date cannot be in the past."); if (repository.existsByDoctorAndSlot(r.doctorId(), r.appointmentDate(), r.appointmentTime(), ignoredId)) throw new BadRequestException("The selected time slot is already booked."); }
}
