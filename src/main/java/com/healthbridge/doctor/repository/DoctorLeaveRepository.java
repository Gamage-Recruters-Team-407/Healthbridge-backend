package com.healthbridge.doctor.repository;

import com.healthbridge.doctor.model.DoctorLeave;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface DoctorLeaveRepository extends MongoRepository<DoctorLeave, String> {
    List<DoctorLeave> findByDoctorId(String doctorId);
}
