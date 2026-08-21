package lk.gamage.backend.healthbridgebackend.controller;
import lk.gamage.backend.healthbridgebackend.dto.InvoiceRequest;
import lk.gamage.backend.healthbridgebackend.model.Invoice;
import lk.gamage.backend.healthbridgebackend.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hospital-billing/invoices")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping
    public ResponseEntity<Invoice> createInvoice(
            @RequestBody InvoiceRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(invoiceService.createInvoice(request));
    }

    @GetMapping
    public ResponseEntity<List<Invoice>> getAllInvoices() {

        return ResponseEntity.ok(
                invoiceService.getAllInvoices());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Invoice> getInvoice(
            @PathVariable String id) {

        return ResponseEntity.ok(
                invoiceService.getInvoice(id));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<Invoice>> getPatientInvoices(
            @PathVariable String patientId) {

        return ResponseEntity.ok(
                invoiceService.getPatientInvoices(patientId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Invoice> updateInvoice(
            @PathVariable String id,
            @RequestBody InvoiceRequest request) {

        return ResponseEntity.ok(
                invoiceService.updateInvoice(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvoice(
            @PathVariable String id) {

        invoiceService.deleteInvoice(id);

        return ResponseEntity.noContent().build();
    }
}
