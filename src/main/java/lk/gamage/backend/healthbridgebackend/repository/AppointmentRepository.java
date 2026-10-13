package lk.gamage.backend.healthbridgebackend.repository;

import lk.gamage.backend.healthbridgebackend.model.Appointment;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Aggregation;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

@Repository
public interface AppointmentRepository extends MongoRepository<Appointment, String> {
    List<Appointment> findByDoctorId(String doctorId);
    List<Appointment> findByPatientIdOrderByAppointmentDateDescAppointmentTimeDesc(String patientId);
    List<Appointment> findBySessionIdOrderByAppointmentNumberAsc(String sessionId);
    Optional<Appointment> findByReferenceNumber(String referenceNumber);
    boolean existsBySessionIdAndPatientIdAndStatusIn(String sessionId, String patientId,
            List<lk.gamage.backend.healthbridgebackend.enums.AppointmentStatus> statuses);
    List<Appointment> findByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatus(
            String doctorId, java.time.LocalDate date, String time,
            lk.gamage.backend.healthbridgebackend.enums.AppointmentStatus status);

    @Query(value = "{ '$and': [ { 'appointmentDate': { '$gte': ?0 } }, { 'appointmentDate': { '$lt': ?1 } } ] }", count = true)
    long countByAppointmentDateGreaterThanEqualAndAppointmentDateLessThan(
            LocalDate start, LocalDate end);

    @Aggregation(pipeline = {
            "{ '$match': { '$and': [ { 'appointmentDate': { '$gte': ?0 } }, { 'appointmentDate': { '$lt': ?1 } }, { 'doctorSpecialization': { '$nin': [null, ''] } }, { 'patientId': { '$nin': [null, ''] } } ] } }",
            "{ '$group': { '_id': { 'department': '$doctorSpecialization', 'status': '$status' }, 'patientIds': { '$addToSet': '$patientId' }, 'appointmentCount': { '$sum': 1 }, 'completedCount': { '$sum': { '$cond': [ { '$eq': ['$status', 'COMPLETED'] }, 1, 0 ] } } } }",
            "{ '$project': { '_id': 0, 'department': '$_id.department', 'status': '$_id.status', 'patientIds': 1, 'appointmentCount': 1, 'completedCount': 1 } }"
    })
    List<DepartmentAppointmentStats> aggregateDepartmentAppointments(LocalDate start, LocalDate end);

    record DepartmentAppointmentStats(
            String department,
            String status,
            List<String> patientIds,
            long appointmentCount,
            long completedCount
    ) {
    }

    default boolean existsByDoctorAndSlot(String doctorId, java.time.LocalDate date, String time, String ignoredId) {
        return findAll().stream().anyMatch(a -> !a.getId().equals(ignoredId)
            && doctorId.equals(a.getDoctorId()) && date.equals(a.getAppointmentDate())
            && time.equals(a.getAppointmentTime())
            && a.getStatus() == lk.gamage.backend.healthbridgebackend.enums.AppointmentStatus.UPCOMING);
    }
}
