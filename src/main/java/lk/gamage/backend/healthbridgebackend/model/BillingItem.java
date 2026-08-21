package lk.gamage.backend.healthbridgebackend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "billing_items")
public class BillingItem {

    @Id
    private String id;

    private String invoiceId;

    private String itemCode;

    private String description;

    private String category;

    private Integer quantity;

    private BigDecimal unitPrice;

    private BigDecimal totalPrice;
}