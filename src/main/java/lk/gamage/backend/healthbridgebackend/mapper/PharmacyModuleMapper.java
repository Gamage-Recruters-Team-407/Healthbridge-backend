package lk.gamage.backend.healthbridgebackend.mapper;

import lk.gamage.backend.healthbridgebackend.dto.DeliveryDto;
import lk.gamage.backend.healthbridgebackend.dto.InventoryRequest;
import lk.gamage.backend.healthbridgebackend.dto.InventoryResponse;
import lk.gamage.backend.healthbridgebackend.dto.MedicineDto;
import lk.gamage.backend.healthbridgebackend.dto.PharmacyDTOs;
import lk.gamage.backend.healthbridgebackend.model.Delivery;
import lk.gamage.backend.healthbridgebackend.model.Inventory;
import lk.gamage.backend.healthbridgebackend.model.Medicine;
import lk.gamage.backend.healthbridgebackend.model.Pharmacy;
import lk.gamage.backend.healthbridgebackend.model.enums.DeliveryStatus;
import lk.gamage.backend.healthbridgebackend.model.enums.InventoryStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;


public class PharmacyModuleMapper {

    // ================= Pharmacy =================

    public static class PharmacyMapper {

        public static Pharmacy toEntity(PharmacyDTOs.Request request) {
            return Pharmacy.builder()
                    .registrationNumber(request.getRegistrationNumber())
                    .name(request.getName())
                    .licenseNumber(request.getLicenseNumber())
                    .contactPerson(request.getContactPerson())
                    .phoneNumber(request.getPhoneNumber())
                    .email(request.getEmail())
                    .address(request.getAddress())
                    .city(request.getCity())
                    .operatingHours(request.getOperatingHours())
                    .active(true)
                    .approved(false)
                    .build();
        }

        public static PharmacyDTOs.Response toResponse(Pharmacy pharmacy) {
            PharmacyDTOs.Response response = new PharmacyDTOs.Response();
            response.setId(pharmacy.getId());
            response.setRegistrationNumber(pharmacy.getRegistrationNumber());
            response.setName(pharmacy.getName());
            response.setLicenseNumber(pharmacy.getLicenseNumber());
            response.setContactPerson(pharmacy.getContactPerson());
            response.setPhoneNumber(pharmacy.getPhoneNumber());
            response.setEmail(pharmacy.getEmail());
            response.setAddress(pharmacy.getAddress());
            response.setCity(pharmacy.getCity());
            response.setActive(pharmacy.isActive());
            response.setApproved(pharmacy.isApproved());
            return response;
        }
    }

    // ================= Medicine =================

    public static class MedicineMapper {

        public static Medicine toEntity(MedicineDto.Request request) {
            return Medicine.builder()
                    .medicineCode(request.getMedicineCode())
                    .name(request.getName())
                    .genericName(request.getGenericName())
                    .brand(request.getBrand())
                    .manufacturer(request.getManufacturer())
                    .category(request.getCategory())
                    .dosageForm(request.getDosageForm())
                    .strength(request.getStrength())
                    .controlledDrug(request.isControlledDrug())
                    .prescriptionRequired(request.isPrescriptionRequired())
                    .unitPrice(request.getUnitPrice())
                    .build();
        }

        public static MedicineDto.Response toResponse(Medicine medicine) {
            MedicineDto.Response response = new MedicineDto.Response();
            response.setId(medicine.getId());
            response.setMedicineCode(medicine.getMedicineCode());
            response.setName(medicine.getName());
            response.setGenericName(medicine.getGenericName());
            response.setBrand(medicine.getBrand());
            response.setCategory(medicine.getCategory());
            response.setDosageForm(medicine.getDosageForm());
            response.setStrength(medicine.getStrength());
            response.setPrescriptionRequired(medicine.isPrescriptionRequired());
            response.setUnitPrice(medicine.getUnitPrice());
            return response;
        }
    }

    // ================= Inventory =================

    public static class InventoryMapper {

        public static Inventory toEntity(InventoryRequest request) {
            return Inventory.builder()
                    .pharmacyId(request.getPharmacyId())
                    .medicineId(request.getMedicineId())
                    .medicineCode(request.getItemCode())
                    .medicineName(request.getItemName())
                    .batchNumber(request.getBatchNumber())
                    .supplierId(request.getSupplierId())
                    .supplierName(request.getSupplier())
                    .quantityInStock(request.getQuantity() != null ? request.getQuantity() : 0)
                    .reorderLevel(request.getMinimumStock() != null ? request.getMinimumStock() : 0)
                    .reorderQuantity(request.getReorderQuantity() != null ? request.getReorderQuantity() : 0)
                    .costPrice(request.getUnitCost() != null ? request.getUnitCost() : 0)
                    .sellingPrice(request.getSellingPrice() != null ? request.getSellingPrice() : 0)
                    .manufactureDate(request.getManufactureDate())
                    .expiryDate(request.getExpiryDate())
                    .status(resolveStatus(request.getQuantity(), request.getMinimumStock()))
                    .build();
        }

        public static InventoryResponse toResponse(Inventory inv) {
            InventoryResponse r = new InventoryResponse();
            r.setId(inv.getId());
            r.setItemCode(inv.getMedicineCode());
            r.setItemName(inv.getMedicineName());
            r.setPharmacyId(inv.getPharmacyId());
            r.setMedicineId(inv.getMedicineId());
            r.setBatchNumber(inv.getBatchNumber());
            r.setSupplierName(inv.getSupplierName());
            r.setQuantity(inv.getQuantityInStock());
            r.setMinimumStock(inv.getReorderLevel());
            r.setUnitCost(inv.getCostPrice());
            r.setSellingPrice(inv.getSellingPrice());
            r.setExpiryDate(inv.getExpiryDate());
            r.setStatus(inv.getStatus() != null ? inv.getStatus().name() : null);
            return r;
        }

        public static InventoryStatus resolveStatus(Integer quantity, Integer reorderLevel) {
            int q = quantity != null ? quantity : 0;
            int r = reorderLevel != null ? reorderLevel : 0;
            if (q <= 0) return InventoryStatus.OUT_OF_STOCK;
            if (q <= r) return InventoryStatus.LOW_STOCK;
            return InventoryStatus.IN_STOCK;
        }
    }

    // ================= Delivery =================

    public static class DeliveryMapper {

        public static Delivery toEntity(DeliveryDto.Request request) {
            return Delivery.builder()
                    .deliveryCode("DLV-" + LocalDate.now().toString().replace("-", "")
                            + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase())
                    .pharmacyId(request.getPharmacyId())
                    .patientId(request.getPatientId())
                    .prescriptionId(request.getPrescriptionId())
                    .items(request.getItems().stream()
                            .map(i -> Delivery.DeliveryItem.builder()
                                    .medicineId(i.getMedicineId())
                                    .medicineName(i.getMedicineName())
                                    .quantity(i.getQuantity())
                                    .build())
                            .collect(Collectors.toList()))
                    .deliveryAddress(request.getDeliveryAddress())
                    .contactPhone(request.getContactPhone())
                    .status(DeliveryStatus.PENDING)
                    .scheduledAt(LocalDateTime.now())
                    .build();
        }

        public static DeliveryDto.Response toResponse(Delivery d) {
            DeliveryDto.Response r = new DeliveryDto.Response();
            r.setId(d.getId());
            r.setDeliveryCode(d.getDeliveryCode());
            r.setPharmacyId(d.getPharmacyId());
            r.setPatientId(d.getPatientId());
            r.setItems(d.getItems().stream()
                    .map(i -> {
                        DeliveryDto.ItemDTO dto = new DeliveryDto.ItemDTO();
                        dto.setMedicineId(i.getMedicineId());
                        dto.setMedicineName(i.getMedicineName());
                        dto.setQuantity(i.getQuantity());
                        return dto;
                    })
                    .collect(Collectors.toList()));
            r.setDeliveryAddress(d.getDeliveryAddress());
            r.setStatus(d.getStatus().name());
            r.setAssignedRiderName(d.getAssignedRiderName());
            return r;
        }
    }
}