package lk.gamage.backend.healthbridgebackend.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class InventoryRequest {

    private String itemCode;

    private String itemName;

    private String category;

    private String supplier;

    private Integer quantity;

    private Integer minimumStock;

    private String unit;

    private Double unitCost;

    private LocalDate expiryDate;
}
