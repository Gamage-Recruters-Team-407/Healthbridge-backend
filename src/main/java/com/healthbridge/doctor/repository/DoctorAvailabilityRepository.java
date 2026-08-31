package com.healthbridge.doctor.repository;

import com.healthbridge.doctor.model.DoctorAvailability;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface DoctorAvailabilityRepository extends MongoRepository<DoctorAvailability, String> {
    List<DoctorAvailability> findByDoctorId(String doctorId);
}
