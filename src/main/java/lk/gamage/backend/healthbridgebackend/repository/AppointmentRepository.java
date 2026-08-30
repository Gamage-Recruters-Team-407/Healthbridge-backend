package lk.gamage.backend.healthbridgebackend.repository;

import lk.gamage.backend.healthbridgebackend.model.Appointment;
import org.springframework.stereotype.Repository;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class AppointmentRepository {
    private final Map<String, Appointment> appointments = new ConcurrentHashMap<>();
    public List<Appointment> findAll() { return new ArrayList<>(appointments.values()); }
    public Optional<Appointment> findById(String id) { return Optional.ofNullable(appointments.get(id)); }
    public Appointment save(Appointment appointment) { appointments.put(appointment.getId(), appointment); return appointment; }
    public void deleteById(String id) { appointments.remove(id); }
    public boolean existsByDoctorAndSlot(String doctorId, java.time.LocalDate date, String time, String ignoredId) {
        return appointments.values().stream().anyMatch(a -> !a.getId().equals(ignoredId)
            && doctorId.equals(a.getDoctorId()) && date.equals(a.getAppointmentDate())
            && time.equals(a.getAppointmentTime())
            && a.getStatus() == lk.gamage.backend.healthbridgebackend.enums.AppointmentStatus.UPCOMING);
    }
}
