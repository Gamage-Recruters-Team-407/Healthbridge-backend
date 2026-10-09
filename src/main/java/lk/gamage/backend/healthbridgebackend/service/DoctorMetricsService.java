package lk.gamage.backend.healthbridgebackend.service;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import lk.gamage.backend.healthbridgebackend.model.Appointment;
import lk.gamage.backend.healthbridgebackend.model.Invoice;
import lk.gamage.backend.healthbridgebackend.enums.AppointmentStatus;
import lk.gamage.backend.healthbridgebackend.repository.AppointmentRepository;
import lk.gamage.backend.healthbridgebackend.repository.InvoiceRepository;
import org.springframework.stereotype.Service;

@Service
public class DoctorMetricsService {
    private final AppointmentRepository appointments;
    private final InvoiceRepository invoices;
    public DoctorMetricsService(AppointmentRepository appointments, InvoiceRepository invoices) {
        this.appointments = appointments; this.invoices = invoices;
    }
    public record Revenue(String month, BigDecimal amount) {}
    public record PaymentRow(String id, String patientName, String date, String service, BigDecimal amount, String status) {}
    public record Earnings(BigDecimal totalEarnings, BigDecimal monthlyEarnings, BigDecimal consultationIncome,
            BigDecimal pendingAmount, long completedConsultations, long todayAppointments,
            List<Revenue> revenue, List<PaymentRow> payments) {}
    public Earnings get(String doctorId) {
        return summarize(appointments.findByDoctorId(doctorId), invoices.findByDoctorId(doctorId),
                LocalDate.now(ZoneId.of("Asia/Colombo")));
    }
    static Earnings summarize(List<Appointment> appointments, List<Invoice> invoices, LocalDate today) {
        BigDecimal total = BigDecimal.ZERO, monthly = BigDecimal.ZERO, pending = BigDecimal.ZERO;
        Map<YearMonth, BigDecimal> buckets = new TreeMap<>();
        List<PaymentRow> rows = new ArrayList<>();
        YearMonth current = YearMonth.from(today);
        for (Invoice invoice : invoices) {
            if (!"APPOINTMENT".equalsIgnoreCase(invoice.getInvoiceSource())
                    || "CANCELLED".equalsIgnoreCase(invoice.getStatus()) || "DRAFT".equalsIgnoreCase(invoice.getStatus())
                    || "REFUNDED".equalsIgnoreCase(invoice.getPaymentStatus())) continue;
            BigDecimal paid = invoice.getPaidAmount() == null ? BigDecimal.ZERO : invoice.getPaidAmount().max(BigDecimal.ZERO);
            BigDecimal balance = invoice.getBalance() == null ? BigDecimal.ZERO : invoice.getBalance().max(BigDecimal.ZERO);
            total = total.add(paid); pending = pending.add(balance);
            if (invoice.getIssueDate() != null) {
                YearMonth month = YearMonth.from(invoice.getIssueDate());
                buckets.merge(month, paid, BigDecimal::add);
                if (month.equals(current)) monthly = monthly.add(paid);
            }
            if (paid.signum() > 0) rows.add(new PaymentRow(invoice.getId(), invoice.getPatientName(),
                    invoice.getIssueDate() == null ? "" : invoice.getIssueDate().toLocalDate().toString(),
                    "Consultation", paid, "Paid"));
        }
        List<Revenue> revenue = new ArrayList<>();
        for (int offset = 5; offset >= 0; offset--) {
            YearMonth month = current.minusMonths(offset);
            revenue.add(new Revenue(month.toString(), buckets.getOrDefault(month, BigDecimal.ZERO)));
        }
        long completed = appointments.stream().filter(a -> a.getStatus() == AppointmentStatus.COMPLETED).count();
        long todays = appointments.stream().filter(a -> today.equals(a.getAppointmentDate()))
                .filter(a -> a.getStatus() != null && (a.getStatus().isActive() || a.getStatus() == AppointmentStatus.COMPLETED)).count();
        return new Earnings(total, monthly, monthly, pending, completed, todays, revenue, rows);
    }
}
