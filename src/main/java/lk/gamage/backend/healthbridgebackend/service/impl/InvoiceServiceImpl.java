package lk.gamage.backend.healthbridgebackend.service.impl;

import lk.gamage.backend.healthbridgebackend.dto.request.InvoiceRequest;
import lk.gamage.backend.healthbridgebackend.dto.response.InvoiceResponse;
import lk.gamage.backend.healthbridgebackend.model.Invoice;
import lk.gamage.backend.healthbridgebackend.repository.InvoiceRepository;
import lk.gamage.backend.healthbridgebackend.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;

    @Override
    public InvoiceResponse createInvoice(InvoiceRequest request) {

        Invoice invoice = new Invoice();

        invoice.setInvoiceNumber(generateInvoiceNumber());

        invoice.setPatientId(request.getPatientId());
        invoice.setPatientName(request.getPatientName());
        invoice.setHospitalId(request.getHospitalId());

        invoice.setIssueDate(
                request.getIssueDate() != null
                        ? request.getIssueDate()
                        : LocalDateTime.now()
        );

        invoice.setDueDate(request.getDueDate());

        invoice.setSubtotal(BigDecimal.ZERO);

        invoice.setDiscount(
                request.getDiscount() != null
                        ? request.getDiscount()
                        : BigDecimal.ZERO
        );

        invoice.setTax(
                request.getTax() != null
                        ? request.getTax()
                        : BigDecimal.ZERO
        );

        invoice.setTotal(BigDecimal.ZERO);

        invoice.setPaidAmount(
                request.getPaidAmount() != null
                        ? request.getPaidAmount()
                        : BigDecimal.ZERO
        );

        invoice.setBalance(BigDecimal.ZERO);

        invoice.setStatus("DRAFT");

        invoice.setPaymentStatus("UNPAID");

        invoice.setNotes(request.getNotes());

        Invoice savedInvoice = invoiceRepository.save(invoice);

        return mapToResponse(savedInvoice);
    }

    @Override
    public List<InvoiceResponse> getAllInvoices() {

        return invoiceRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public InvoiceResponse getInvoice(String id) {

        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Invoice not found with id: " + id)
                );

        return mapToResponse(invoice);
    }

    @Override
    public List<InvoiceResponse> getPatientInvoices(String patientId) {

        return invoiceRepository.findByPatientId(patientId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public InvoiceResponse updateInvoice(
            String id,
            InvoiceRequest request) {

        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Invoice not found with id: " + id)
                );

        invoice.setPatientId(request.getPatientId());
        invoice.setPatientName(request.getPatientName());
        invoice.setHospitalId(request.getHospitalId());

        if (request.getIssueDate() != null) {
            invoice.setIssueDate(request.getIssueDate());
        }

        invoice.setDueDate(request.getDueDate());

        invoice.setDiscount(
                request.getDiscount() != null
                        ? request.getDiscount()
                        : BigDecimal.ZERO
        );

        invoice.setTax(
                request.getTax() != null
                        ? request.getTax()
                        : BigDecimal.ZERO
        );

        invoice.setPaidAmount(
                request.getPaidAmount() != null
                        ? request.getPaidAmount()
                        : BigDecimal.ZERO
        );

        invoice.setNotes(request.getNotes());

        Invoice updatedInvoice = invoiceRepository.save(invoice);

        return mapToResponse(updatedInvoice);
    }

    @Override
    public void deleteInvoice(String id) {

        if (!invoiceRepository.existsById(id)) {
            throw new RuntimeException(
                    "Invoice not found with id: " + id
            );
        }

        invoiceRepository.deleteById(id);
    }

    private String generateInvoiceNumber() {

        return "INV-" +
                LocalDateTime.now()
                        .format(
                                DateTimeFormatter.ofPattern(
                                        "yyyyMMddHHmmssSSS"
                                )
                        );
    }

    private InvoiceResponse mapToResponse(Invoice invoice) {

        return InvoiceResponse.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .patientId(invoice.getPatientId())
                .patientName(invoice.getPatientName())
                .hospitalId(invoice.getHospitalId())
                .issueDate(invoice.getIssueDate())
                .dueDate(invoice.getDueDate())
                .subtotal(invoice.getSubtotal())
                .discount(invoice.getDiscount())
                .tax(invoice.getTax())
                .total(invoice.getTotal())
                .paidAmount(invoice.getPaidAmount())
                .balance(invoice.getBalance())
                .status(invoice.getStatus())
                .paymentStatus(invoice.getPaymentStatus())
                .notes(invoice.getNotes())
                .build();
    }
}