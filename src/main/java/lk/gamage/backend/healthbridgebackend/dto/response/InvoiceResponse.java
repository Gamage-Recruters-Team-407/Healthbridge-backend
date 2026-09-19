package lk.gamage.backend.healthbridgebackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponse {

    private String id;

    private String invoiceNumber;

    private String patientId;

    private String patientName;

    private String hospitalId;

    private LocalDateTime issueDate;

    private LocalDateTime dueDate;

    private BigDecimal subtotal;

    private BigDecimal discount;

    private BigDecimal tax;

    private BigDecimal total;

    private BigDecimal paidAmount;

    private BigDecimal balance;

    private String status;

    private String paymentStatus;

    private String notes;
}