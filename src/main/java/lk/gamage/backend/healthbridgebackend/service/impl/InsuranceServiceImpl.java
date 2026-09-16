package lk.gamage.backend.healthbridgebackend.service.impl;

import lk.gamage.backend.healthbridgebackend.dto.*;
import lk.gamage.backend.healthbridgebackend.dto.request.ClaimDecisionRequest;
import lk.gamage.backend.healthbridgebackend.dto.request.InsuranceClaimRequest;
import lk.gamage.backend.healthbridgebackend.dto.request.InsurancePolicyRequest;
import lk.gamage.backend.healthbridgebackend.dto.response.InsuranceClaimResponse;
import lk.gamage.backend.healthbridgebackend.dto.response.InsurancePolicyResponse;
import lk.gamage.backend.healthbridgebackend.enums.ClaimStatus;
import lk.gamage.backend.healthbridgebackend.enums.PolicyStatus;
import lk.gamage.backend.healthbridgebackend.exception.BadRequestException;
import lk.gamage.backend.healthbridgebackend.exception.ResourceNotFoundException;
import lk.gamage.backend.healthbridgebackend.exception.UnauthorizedException;
import lk.gamage.backend.healthbridgebackend.model.InsuranceClaim;
import lk.gamage.backend.healthbridgebackend.model.InsurancePolicy;
import lk.gamage.backend.healthbridgebackend.repository.InsuranceClaimRepository;
import lk.gamage.backend.healthbridgebackend.repository.InsurancePolicyRepository;
import lk.gamage.backend.healthbridgebackend.service.FileStorageService;
import lk.gamage.backend.healthbridgebackend.service.InsuranceService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class InsuranceServiceImpl implements InsuranceService {

    private final InsurancePolicyRepository policyRepo;
    private final InsuranceClaimRepository claimRepo;
    private final FileStorageService fileStorageService;

    public InsuranceServiceImpl(InsurancePolicyRepository policyRepo,
                                 InsuranceClaimRepository claimRepo,
                                 FileStorageService fileStorageService) {
        this.policyRepo = policyRepo;
        this.claimRepo = claimRepo;
        this.fileStorageService = fileStorageService;
    }

    @Override
    public InsurancePolicyResponse createPolicy(InsurancePolicyRequest req) {
        if (policyRepo.existsByPolicyNumber(req.getPolicyNumber())) {
            throw new BadRequestException("Policy number already exists");
        }
        if (!req.getEndDate().isAfter(req.getStartDate())) {
            throw new BadRequestException("End date must be after start date");
        }
        InsurancePolicy policy = InsurancePolicy.builder()
                .policyNumber(req.getPolicyNumber())
                .patientId(req.getPatientId())
                .providerName(req.getProviderName())
                .policyType(req.getPolicyType())
                .coverageAmount(req.getCoverageAmount())
                .coverageUsed(0.0)
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .status(PolicyStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return toInsurancePolicyResponse(policyRepo.save(policy));
    }

    @Override
    public InsurancePolicyResponse getPolicyById(String policyId) {
        return toInsurancePolicyResponse(findPolicyOrThrow(policyId));
    }

    @Override
    public List<InsurancePolicyResponse> getPoliciesForPatient(String patientId) {
        return policyRepo.findByPatientId(patientId).stream()
                .map(this::toInsurancePolicyResponse).collect(Collectors.toList());
    }

    @Override
    public InsurancePolicyResponse verifyPolicy(String policyNumber) {
        InsurancePolicy policy = policyRepo.findByPolicyNumber(policyNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found: " + policyNumber));
        if (policy.getStatus() != PolicyStatus.ACTIVE) {
            throw new BadRequestException("Policy is not active: " + policy.getStatus());
        }
        if (policy.getEndDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Policy has expired");
        }
        return toInsurancePolicyResponse(policy);
    }

    @Override
    public InsuranceClaimResponse submitClaim(String patientId, InsuranceClaimRequest req, List<MultipartFile> documents) {
        InsurancePolicy policy = findPolicyOrThrow(req.getPolicyId());

        if (!policy.getPatientId().equals(patientId)) {
            throw new UnauthorizedException("This policy does not belong to you");
        }
        if (policy.getStatus() != PolicyStatus.ACTIVE || policy.getEndDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Policy is not active or has expired");
        }
        // SRS 8.2.5: claim amount cannot exceed policy limits
        double remaining = policy.getCoverageAmount() - policy.getCoverageUsed();
        if (req.getClaimAmount() > remaining) {
            throw new BadRequestException(
                    "Claim amount exceeds remaining policy coverage (remaining: " + remaining + ")");
        }
        // SRS 8.2.5: supporting documents must be attached
        if (documents == null || documents.isEmpty()) {
            throw new BadRequestException("At least one supporting document is required");
        }

        List<String> fileIds = documents.stream()
                .map(fileStorageService::store)
                .collect(Collectors.toList());

        InsuranceClaim claim = InsuranceClaim.builder()
                .claimNumber("CLM-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .policyId(policy.getId())
                .patientId(patientId)
                .treatmentDescription(req.getTreatmentDescription())
                .claimAmount(req.getClaimAmount())
                .documentFileIds(fileIds)
                .status(ClaimStatus.SUBMITTED)
                .submittedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return toInsuranceClaimResponse(claimRepo.save(claim));
    }

    @Override
    public InsuranceClaimResponse getClaimById(String claimId, String requesterId, boolean isOfficer) {
        InsuranceClaim claim = findClaimOrThrow(claimId);
        if (!isOfficer && !claim.getPatientId().equals(requesterId)) {
            throw new UnauthorizedException("You do not have access to this claim");
        }
        return toInsuranceClaimResponse(claim);
    }

    @Override
    public List<InsuranceClaimResponse> getClaimsForPatient(String patientId) {
        return claimRepo.findByPatientId(patientId).stream()
                .map(this::toInsuranceClaimResponse).collect(Collectors.toList());
    }

    @Override
    public List<InsuranceClaimResponse> getAllClaims() {
        return claimRepo.findAll().stream()
                .map(this::toInsuranceClaimResponse).collect(Collectors.toList());
    }

    @Override
    public InsuranceClaimResponse decideClaim(String claimId, String officerId, ClaimDecisionRequest decision) {
        InsuranceClaim claim = findClaimOrThrow(claimId);
        if (claim.getStatus() != ClaimStatus.SUBMITTED && claim.getStatus() != ClaimStatus.UNDER_REVIEW) {
            throw new BadRequestException("Claim has already been decided");
        }

        if (Boolean.TRUE.equals(decision.getApprove())) {
            if (decision.getApprovedAmount() == null || decision.getApprovedAmount() <= 0) {
                throw new BadRequestException("Approved amount is required for approval");
            }
            InsurancePolicy policy = findPolicyOrThrow(claim.getPolicyId());
            double remaining = policy.getCoverageAmount() - policy.getCoverageUsed();
            if (decision.getApprovedAmount() > remaining) {
                throw new BadRequestException("Approved amount exceeds remaining policy coverage");
            }
            policy.setCoverageUsed(policy.getCoverageUsed() + decision.getApprovedAmount());
            policy.setUpdatedAt(LocalDateTime.now());
            policyRepo.save(policy);

            claim.setStatus(ClaimStatus.APPROVED);
            claim.setApprovedAmount(decision.getApprovedAmount());
        } else {
            if (decision.getRejectionReason() == null || decision.getRejectionReason().isBlank()) {
                throw new BadRequestException("Rejection reason is required");
            }
            claim.setStatus(ClaimStatus.REJECTED);
            claim.setRejectionReason(decision.getRejectionReason());
        }

        claim.setReviewedByOfficerId(officerId);
        claim.setReviewedAt(LocalDateTime.now());
        claim.setUpdatedAt(LocalDateTime.now());

        return toInsuranceClaimResponse(claimRepo.save(claim));
    }

    // --- helpers ---

    private InsurancePolicy findPolicyOrThrow(String id) {
        return policyRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found: " + id));
    }

    private InsuranceClaim findClaimOrThrow(String id) {
        return claimRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found: " + id));
    }

    private InsurancePolicyResponse toInsurancePolicyResponse(InsurancePolicy p) {
        return InsurancePolicyResponse.builder()
                .id(p.getId())
                .policyNumber(p.getPolicyNumber())
                .patientId(p.getPatientId())
                .providerName(p.getProviderName())
                .policyType(p.getPolicyType())
                .coverageAmount(p.getCoverageAmount())
                .coverageUsed(p.getCoverageUsed())
                .coverageRemaining(p.getCoverageAmount() - p.getCoverageUsed())
                .startDate(p.getStartDate())
                .endDate(p.getEndDate())
                .status(p.getStatus())
                .build();
    }

    private InsuranceClaimResponse toInsuranceClaimResponse(InsuranceClaim c) {
        return InsuranceClaimResponse.builder()
                .id(c.getId())
                .claimNumber(c.getClaimNumber())
                .policyId(c.getPolicyId())
                .patientId(c.getPatientId())
                .treatmentDescription(c.getTreatmentDescription())
                .claimAmount(c.getClaimAmount())
                .approvedAmount(c.getApprovedAmount())
                .documentFileIds(c.getDocumentFileIds())
                .status(c.getStatus())
                .rejectionReason(c.getRejectionReason())
                .submittedAt(c.getSubmittedAt())
                .reviewedAt(c.getReviewedAt())
                .build();
    }
}