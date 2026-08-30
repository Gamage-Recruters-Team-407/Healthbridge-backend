package lk.gamage.backend.healthbridgebackend.dto.response;

import lk.gamage.backend.healthbridgebackend.enums.ClaimStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class InsuranceClaimResponse {
    private String id;
    private String claimNumber;
    private String policyId;
    private String patientId;
    private String treatmentDescription;
    private Double claimAmount;
    private Double approvedAmount;
    private List<String> documentFileIds;
    private ClaimStatus status;
    private String rejectionReason;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    
}
