package lk.gamage.backend.healthbridgebackend.service.impl;
import lk.gamage.backend.healthbridgebackend.dto.BillingItemRequest;
import lk.gamage.backend.healthbridgebackend.dto.InvoiceRequest;
import lk.gamage.backend.healthbridgebackend.model.BillingItem;
import lk.gamage.backend.healthbridgebackend.model.Invoice;
import lk.gamage.backend.healthbridgebackend.model.enums.InvoiceStatus;
import lk.gamage.backend.healthbridgebackend.model.enums.PaymentStatus;
import lk.gamage.backend.healthbridgebackend.repository.BillingItemRepository;
import lk.gamage.backend.healthbridgebackend.repository.InvoiceRepository;
import lk.gamage.backend.healthbridgebackend.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final BillingItemRepository billingItemRepository;

    @Override
    public Invoice createInvoice(InvoiceRequest request) {

        BigDecimal subTotal = BigDecimal.ZERO;

        for (BillingItemRequest item : request.getItems()) {

            BigDecimal total = item.getUnitPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));

            subTotal = subTotal.add(total);
        }

        BigDecimal discount =
                request.getDiscount() == null
                        ? BigDecimal.ZERO
                        : request.getDiscount();

        BigDecimal tax =
                request.getTax() == null
                        ? BigDecimal.ZERO
                        : request.getTax();

        BigDecimal total =
                subTotal.subtract(discount).add(tax);

        Invoice invoice = Invoice.builder()
                .invoiceNumber("INV-" +
                        UUID.randomUUID()
                                .toString()
                                .substring(0, 8)
                                .toUpperCase())
                .patientId(request.getPatientId())
                .patientName(request.getPatientName())
                .hospitalId(request.getHospitalId())
                .subTotal(subTotal)
                .discount(discount)
                .tax(tax)
                .totalAmount(total)
                .status(InvoiceStatus.ISSUED)
                .paymentStatus(PaymentStatus.UNPAID)
                .invoiceDate(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Invoice saved = invoiceRepository.save(invoice);

        for (BillingItemRequest item : request.getItems()) {

            BigDecimal itemTotal =
                    item.getUnitPrice()
                            .multiply(
                                    BigDecimal.valueOf(
                                            item.getQuantity()));

            BillingItem billingItem =
                    BillingItem.builder()
                            .invoiceId(saved.getId())
                            .itemCode(item.getItemCode())
                            .description(item.getDescription())
                            .category(item.getCategory())
                            .quantity(item.getQuantity())
                            .unitPrice(item.getUnitPrice())
                            .totalPrice(itemTotal)
                            .build();

            billingItemRepository.save(billingItem);
        }

        return saved;
    }

    @Override
    public Invoice getInvoice(String id) {

        return invoiceRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Invoice not found"));
    }

    @Override
    public List<Invoice> getAllInvoices() {

        return invoiceRepository.findAll();
    }

    @Override
    public List<Invoice> getPatientInvoices(String patientId) {

        return invoiceRepository.findByPatientId(patientId);
    }

    @Override
    public Invoice updateInvoice(
            String id,
            InvoiceRequest request) {

        Invoice invoice = getInvoice(id);

        invoice.setPatientId(request.getPatientId());
        invoice.setPatientName(request.getPatientName());
        invoice.setHospitalId(request.getHospitalId());
        invoice.setUpdatedAt(LocalDateTime.now());

        return invoiceRepository.save(invoice);
    }

    @Override
    public void deleteInvoice(String id) {

        billingItemRepository.deleteByInvoiceId(id);

        invoiceRepository.deleteById(id);
    }
}
