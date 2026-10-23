package lk.gamage.backend.healthbridgebackend.repository;

import lk.gamage.backend.healthbridgebackend.model.ConsultationSession;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;
import java.time.LocalDateTime;

public interface ConsultationSessionRepository extends MongoRepository<ConsultationSession, String> {

    Optional<ConsultationSession> findByTelemedicineSessionId(String telemedicineSessionId);

    @Query(value = "{ '$and': [ { 'createdAt': { '$gte': ?0 } }, { 'createdAt': { '$lt': ?1 } } ] }", count = true)
    long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            LocalDateTime start, LocalDateTime end);
}
