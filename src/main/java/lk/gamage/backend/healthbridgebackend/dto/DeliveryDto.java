package lk.gamage.backend.healthbridgebackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

public class DeliveryDto {

    @Data
    public static class Request {
        @NotBlank
        private String pharmacyId;

        @NotBlank
        private String patientId;

        private String prescriptionId;

        @NotEmpty
        private List<ItemDTO> items;

        @NotBlank
        private String deliveryAddress;

        @NotBlank
        private String contactPhone;
    }

    @Data
    public static class Response {
        private String id;
        private String deliveryCode;
        private String pharmacyId;
        private String patientId;
        private List<ItemDTO> items;
        private String deliveryAddress;
        private String status;
        private String assignedRiderName;
    }

    @Data
    public static class StatusUpdateRequest {
        @NotBlank
        private String status;

        private String assignedRiderId;
        private String assignedRiderName;
        private String remarks;
    }

    @Data
    public static class ItemDTO {
        private String medicineId;
        private String medicineName;
        private int quantity;
    }
}