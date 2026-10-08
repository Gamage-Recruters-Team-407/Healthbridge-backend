package lk.gamage.backend.healthbridgebackend.repository;

import lk.gamage.backend.healthbridgebackend.model.LabTest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface LabTestRepository extends MongoRepository<LabTest, String> {

    List<LabTest> findByPatientId(String patientId);
    List<LabTest> findByStatus(LabTest.TestStatus status);
    List<LabTest> findByDoctorId(String doctorId);

    @Query(value = "{ '$and': [ { 'requestedAt': { '$gte': ?0 } }, { 'requestedAt': { '$lt': ?1 } }, { 'status': { '$ne': ?2 } } ] }", count = true)
    long countByRequestedAtGreaterThanEqualAndRequestedAtLessThanAndStatusNot(
            LocalDateTime start, LocalDateTime end, LabTest.TestStatus excludedStatus);
}
