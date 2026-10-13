package lk.gamage.backend.healthbridgebackend.repository;

import lk.gamage.backend.healthbridgebackend.model.MedicationReminder;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MedicationReminderRepository extends MongoRepository<MedicationReminder, String> {
    @Query(value = "{ 'patientId' : ?0, 'scheduledDate' : ?1 }", sort = "{ 'scheduledTime' : 1 }")
    List<MedicationReminder> findByPatientIdAndScheduledDateOrderByScheduledTimeAsc(String patientId, LocalDate scheduledDate);
    
    List<MedicationReminder> findByPatientId(String patientId);
}
