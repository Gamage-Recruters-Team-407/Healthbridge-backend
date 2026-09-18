package lk.gamage.backend.healthbridgebackend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "invoices")
public class Invoice {

    @Id
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
    private String status;        // DRAFT, ISSUED, PAID, CANCELLED
    private String paymentStatus; // UNPAID, PARTIAL, PAID, REFUNDED
    private String notes;
}