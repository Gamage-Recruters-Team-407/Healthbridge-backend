package lk.gamage.backend.healthbridgebackend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class InvoiceRequest {

    private String patientId;

    private String patientName;

    private String hospitalId;

    private BigDecimal discount;

    private BigDecimal tax;

    private List<BillingItemRequest> items;
}
