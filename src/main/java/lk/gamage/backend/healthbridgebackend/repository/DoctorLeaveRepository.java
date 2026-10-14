package lk.gamage.backend.healthbridgebackend.repository;

import lk.gamage.backend.healthbridgebackend.model.DoctorLeave;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorLeaveRepository extends MongoRepository<DoctorLeave, String> {
    List<DoctorLeave> findByDoctorIdOrderByCreatedAtDesc(String doctorId);
    List<DoctorLeave> findByDoctorEmailOrderByCreatedAtDesc(String doctorEmail);
}
