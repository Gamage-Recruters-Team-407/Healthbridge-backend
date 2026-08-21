package lk.gamage.backend.healthbridgebackend.model;

import lk.gamage.backend.healthbridgebackend.model.enums.InventoryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "hospital_inventory")
public class HospitalInventory {

    @Id
    private String id;

    private String itemCode;

    private String itemName;

    private String category;

    private String supplier;

    private Integer quantity;

    private Integer minimumStock;

    private String unit;

    private Double unitCost;

    private LocalDate expiryDate;

    private InventoryStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
