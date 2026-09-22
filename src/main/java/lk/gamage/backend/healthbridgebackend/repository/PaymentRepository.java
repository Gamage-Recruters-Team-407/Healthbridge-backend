package lk.gamage.backend.healthbridgebackend.repository;

import lk.gamage.backend.healthbridgebackend.model.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, String> {

    List<Payment> findByPatientIdOrderByCreatedAtDesc(String patientId);

    Optional<Payment> findByIdAndPatientId(String id, String patientId);
}
