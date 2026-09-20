package lk.gamage.backend.healthbridgebackend.repository;

import lk.gamage.backend.healthbridgebackend.enums.ClaimStatus;
import lk.gamage.backend.healthbridgebackend.model.InsuranceClaim;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface InsuranceClaimRepository extends MongoRepository<InsuranceClaim, String> {
    List<InsuranceClaim> findByPatientId(String patientId);
    List<InsuranceClaim> findByPolicyId(String policyId);
    List<InsuranceClaim> findByStatus(ClaimStatus status);
    Optional<InsuranceClaim> findByClaimNumber(String claimNumber);
}