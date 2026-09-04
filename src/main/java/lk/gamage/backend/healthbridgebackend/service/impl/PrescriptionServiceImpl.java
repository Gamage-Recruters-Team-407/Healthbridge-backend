//package lk.gamage.backend.healthbridgebackend.service.impl;
//
//import lk.gamage.backend.healthbridgebackend.dto.PharmacyDTOs.PrescriptionVerificationRequest;
//import lk.gamage.backend.healthbridgebackend.dto.PharmacyDTOs.PrescriptionVerificationResponse;
//import lk.gamage.backend.healthbridgebackend.dto.PharmacyDTOs.PrescriptionVerificationResponse.ItemAvailability;
//import lk.gamage.backend.healthbridgebackend.exception.ResourceNotFoundException;
//import lk.gamage.backend.healthbridgebackend.exception.BadRequestException;
//import lk.gamage.backend.healthbridgebackend.model.Prescription;
//import lk.gamage.backend.healthbridgebackend.repository.PrescriptionRepository;
//import lk.gamage.backend.healthbridgebackend.repository.InventoryRepository;
//import lk.gamage.backend.healthbridgebackend.service.PrescriptionService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class PrescriptionServiceImpl implements PrescriptionService {
//
//    private final PrescriptionRepository prescriptionRepository;
//    private final InventoryRepository inventoryRepository;
//
//    @Override
//    public PrescriptionVerificationResponse verify(PrescriptionVerificationRequest request) {
//
//        Prescription prescription = resolvePrescription(request);
//
//        PrescriptionVerificationResponse response = new PrescriptionVerificationResponse();
//
//        String failReason = checkValidity(prescription);
//        if (failReason != null) {
//            response.setValid(false);
//            response.setMessage(failReason);
//            response.setPrescriptionId(prescription.getId());
//            response.setStatus(prescription.getStatus());
//            return response;
//        }
//
//        List<ItemAvailability> availabilities = prescription.getItems().stream()
//                .map(item -> buildAvailability(request.getPharmacyId(), item))
//                .collect(Collectors.toList());
//
//        response.setValid(true);
//        response.setMessage("Prescription verified successfully.");
//        response.setPrescriptionId(prescription.getId());
//        response.setPatientName(prescription.getPatientName());
//        response.setStatus(prescription.getStatus());
//        response.setExpiresAt(prescription.getExpiresAt());
//        response.setItems(availabilities);
//
//        return response;
//    }
//
//    private Prescription resolvePrescription(PrescriptionVerificationRequest request) {
//        if (request.getQrToken() != null && !request.getQrToken().isBlank()) {
//            return prescriptionRepository.findByQrToken(request.getQrToken())
//                    .orElseThrow(() -> new BadRequestException("Invalid or unrecognized QR code."));
//        }
//        if (request.getPrescriptionCode() != null && !request.getPrescriptionCode().isBlank()) {
//            return prescriptionRepository.findByPrescriptionCode(request.getPrescriptionCode())
//                    .orElseThrow(() -> new ResourceNotFoundException("Prescription not found."));
//        }
//        throw new BadRequestException("QR token or prescription code is required.");
//    }
//
//    private String checkValidity(Prescription prescription) {
//        if ("CANCELLED".equals(prescription.getStatus())) return "This prescription has been cancelled.";
//        if ("FULLY_DISPENSED".equals(prescription.getStatus())) return "This prescription has already been fully dispensed.";
//        if (prescription.getExpiresAt() != null && prescription.getExpiresAt().isBefore(LocalDateTime.now())) {
//            return "This prescription has expired.";
//        }
//        return null;
//    }
//
//    private ItemAvailability buildAvailability(String pharmacyId, Prescription.PrescriptionItem item) {
//        int remaining = item.getPrescribedQuantity() - item.getDispensedQuantity();
//
//        int available = inventoryRepository
//                .findByPharmacyIdAndMedicineId(pharmacyId, item.getMedicineId())
//                .stream()
//                .mapToInt(inv -> inv.getQuantityInStock())
//                .sum();
//
//        ItemAvailability availability = new ItemAvailability();
//        availability.setMedicineId(item.getMedicineId());
//        availability.setMedicineName(item.getMedicineName());
//        availability.setPrescribedQuantity(remaining);
//        availability.setAvailableInStock(available >= remaining);
//        availability.setAvailableQuantity(available);
//        return availability;
//    }
//}