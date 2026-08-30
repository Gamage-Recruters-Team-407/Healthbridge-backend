package lk.gamage.backend.healthbridgebackend.repository;

import lk.gamage.backend.healthbridgebackend.model.Appointment;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public interface AppointmentRepository extends MongoRepository<Appointment, String> {
    public boolean existsByDoctorAndSlot(String doctorId, java.time.LocalDate date, String time, String ignoredId) {
        return findAll().stream().anyMatch(a -> !a.getId().equals(ignoredId)
            && doctorId.equals(a.getDoctorId()) && date.equals(a.getAppointmentDate())
            && time.equals(a.getAppointmentTime())
            && a.getStatus() == lk.gamage.backend.healthbridgebackend.enums.AppointmentStatus.UPCOMING);
    }
}
