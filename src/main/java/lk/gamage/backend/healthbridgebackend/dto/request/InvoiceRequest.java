package lk.gamage.backend.healthbridgebackend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceRequest {

    private String patientId;

    private String patientName;

    private String hospitalId;

    private LocalDateTime issueDate;

    private LocalDateTime dueDate;

    private BigDecimal discount;

    private BigDecimal tax;

    private BigDecimal paidAmount;

    private String notes;
}