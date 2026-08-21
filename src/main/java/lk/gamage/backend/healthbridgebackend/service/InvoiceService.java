package lk.gamage.backend.healthbridgebackend.service;

import lk.gamage.backend.healthbridgebackend.dto.InvoiceRequest;
import lk.gamage.backend.healthbridgebackend.model.Invoice;

import java.util.List;

public interface InvoiceService {

    Invoice createInvoice(InvoiceRequest request);

    Invoice getInvoice(String id);

    List<Invoice> getAllInvoices();

    List<Invoice> getPatientInvoices(String patientId);

    Invoice updateInvoice(String id, InvoiceRequest request);

    void deleteInvoice(String id);
}
