package lk.gamage.backend.healthbridgebackend.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BillingItemRequest {

    private String itemCode;

    private String description;

    private String category;

    private Integer quantity;

    private BigDecimal unitPrice;
}
