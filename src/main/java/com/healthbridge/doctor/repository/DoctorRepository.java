package com.healthbridge.doctor.repository;

import com.healthbridge.doctor.model.Doctor;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.*;

public interface DoctorRepository extends MongoRepository<Doctor, String> {
    Optional<Doctor> findByEmail(String email);
    List<Doctor> findBySpecialization(String specialization);
}
