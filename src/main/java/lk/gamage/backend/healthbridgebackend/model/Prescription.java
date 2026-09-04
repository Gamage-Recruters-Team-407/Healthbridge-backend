package lk.gamage.backend.healthbridgebackend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "prescriptions")
public class Prescription {

    @Id
    private String id;

    @Indexed(unique = true)
    private String prescriptionCode;

    @Indexed(unique = true)
    private String qrToken;

    private String patientId;
    private String patientName;
    private String doctorId;
    private String doctorName;

    private List<PrescriptionItem> items;

    private String status;

    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;

    @CreatedDate
    private LocalDateTime createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrescriptionItem {
        private String medicineId;
        private String medicineName;
        private String dosageInstructions;
        private int prescribedQuantity;
        private int dispensedQuantity;
    }
}