package lk.gamage.backend.healthbridgebackend.repository;

import lk.gamage.backend.healthbridgebackend.model.Payment;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, String> {

    List<Payment> findByPatientIdOrderByCreatedAtDesc(String patientId);

    Optional<Payment> findByIdAndPatientId(String id, String patientId);

    @Aggregation(pipeline = {
            "{ $match: { status: 'CONFIRMED', confirmedAt: { $gte: ?0, $lt: ?1 }, amount: { $ne: null } } }",
            "{ $group: { _id: null, total: { $sum: '$amount' } } }",
            "{ $project: { _id: 0, total: 1 } }"
    })
    List<ConfirmedRevenue> aggregateConfirmedRevenue(LocalDateTime start, LocalDateTime end);

    record ConfirmedRevenue(BigDecimal total) {
    }
}
