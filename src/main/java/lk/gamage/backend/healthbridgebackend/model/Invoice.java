package lk.gamage.backend.healthbridgebackend.model;

import lk.gamage.backend.healthbridgebackend.model.enums.InvoiceStatus;
import lk.gamage.backend.healthbridgebackend.model.enums.PaymentStatus;
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

    private BigDecimal subTotal;

    private BigDecimal discount;

    private BigDecimal tax;

    private BigDecimal totalAmount;

    private InvoiceStatus status;

    private PaymentStatus paymentStatus;

    private LocalDateTime invoiceDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
