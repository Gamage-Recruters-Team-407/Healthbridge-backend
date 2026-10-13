package lk.gamage.backend.healthbridgebackend.service;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import lk.gamage.backend.healthbridgebackend.model.Appointment;
import lk.gamage.backend.healthbridgebackend.model.Invoice;
import lk.gamage.backend.healthbridgebackend.model.Payment;
import lk.gamage.backend.healthbridgebackend.enums.AppointmentStatus;
import lk.gamage.backend.healthbridgebackend.repository.AppointmentRepository;
import lk.gamage.backend.healthbridgebackend.repository.InvoiceRepository;
import lk.gamage.backend.healthbridgebackend.repository.PaymentRepository;
import org.springframework.stereotype.Service;

@Service
public class DoctorMetricsService {
    private final AppointmentRepository appointments;
    private final InvoiceRepository invoices;
    private final PaymentRepository paymentRepository;

    public DoctorMetricsService(AppointmentRepository appointments, InvoiceRepository invoices, PaymentRepository paymentRepository) {
        this.appointments = appointments;
        this.invoices = invoices;
        this.paymentRepository = paymentRepository;
    }

    public record Revenue(String month, BigDecimal amount) {}
    public record PaymentRow(String id, String patientName, String date, String service, BigDecimal amount, String status) {}
    public record Earnings(BigDecimal totalEarnings, BigDecimal monthlyEarnings, BigDecimal consultationIncome,
            BigDecimal pendingAmount, long completedConsultations, long todayAppointments,
            List<Revenue> revenue, List<PaymentRow> payments) {}

    public Earnings get(String doctorId) {
        List<Appointment> docAppointments = appointments.findByDoctorId(doctorId);
        List<Invoice> docInvoices = invoices.findByDoctorId(doctorId);
        List<Payment> allPayments = paymentRepository.findAll();

        Set<String> patientIds = new HashSet<>();
        for (Appointment a : docAppointments) {
            if (a.getPatientId() != null) patientIds.add(a.getPatientId());
        }

        List<Payment> docPayments = allPayments.stream()
                .filter(p -> isPaymentForDoctor(p, doctorId, patientIds))
                .toList();

        return summarize(docAppointments, docInvoices, docPayments,
                LocalDate.now(ZoneId.of("Asia/Colombo")));
    }

    private boolean isPaymentForDoctor(Payment payment, String doctorId, Set<String> patientIds) {
        if (payment == null) return false;

        // Only consultation payments count towards doctor consultation earnings
        boolean isConsultation = "CONSULTATION".equalsIgnoreCase(payment.getCategory())
                || (payment.getDescription() != null && payment.getDescription().toLowerCase().contains("consultation"));
        if (!isConsultation) return false;

        // If explicit doctorId is set, it must match this doctor
        if (payment.getDoctorId() != null && !payment.getDoctorId().isBlank()) {
            return payment.getDoctorId().equals(doctorId);
        }

        // If doctorId is unassigned:
        // Match if patient has an appointment with this doctor, or if it's an unassigned consultation payment
        return true;
    }

    // Overload for backwards compatibility with existing unit tests
    static Earnings summarize(List<Appointment> appointments, List<Invoice> invoices, LocalDate today) {
        return summarize(appointments, invoices, List.of(), today);
    }

    static Earnings summarize(List<Appointment> appointments, List<Invoice> invoices, List<Payment> payments, LocalDate today) {
        BigDecimal total = BigDecimal.ZERO, monthly = BigDecimal.ZERO, pending = BigDecimal.ZERO;
        Map<YearMonth, BigDecimal> buckets = new TreeMap<>();
        List<PaymentRow> rows = new ArrayList<>();
        YearMonth current = YearMonth.from(today);
        Set<String> processedPaymentIds = new HashSet<>();

        // 1. Process payments from Payment repository (payments collection)
        for (Payment payment : payments) {
            if (payment == null) continue;
            boolean isConfirmed = "CONFIRMED".equalsIgnoreCase(payment.getStatus());
            boolean isPending = "PENDING_CONFIRMATION".equalsIgnoreCase(payment.getStatus())
                    || "PENDING".equalsIgnoreCase(payment.getStatus());
            if (!isConfirmed && !isPending) continue;

            BigDecimal amount = payment.getAmount() == null ? BigDecimal.ZERO : payment.getAmount().max(BigDecimal.ZERO);
            LocalDateTime date = payment.getConfirmedAt() != null ? payment.getConfirmedAt() : payment.getCreatedAt();
            String dateStr = date != null ? date.toLocalDate().toString() : "";
            String serviceName = payment.getDescription() != null && !payment.getDescription().isBlank()
                    ? payment.getDescription() : "Doctor Consultation";
            String patientName = payment.getPatientName() != null && !payment.getPatientName().isBlank()
                    ? payment.getPatientName() : "Patient";

            if (isConfirmed) {
                total = total.add(amount);
                if (date != null) {
                    YearMonth month = YearMonth.from(date);
                    buckets.merge(month, amount, BigDecimal::add);
                    if (month.equals(current)) monthly = monthly.add(amount);
                }
                rows.add(new PaymentRow(payment.getId(), patientName, dateStr, serviceName, amount, "Paid"));
            } else {
                pending = pending.add(amount);
                rows.add(new PaymentRow(payment.getId(), patientName, dateStr, serviceName, amount, "Pending"));
            }
            if (payment.getId() != null) {
                processedPaymentIds.add(payment.getId());
            }
        }

        // 2. Process invoices (for appointment invoices not already recorded via payments)
        for (Invoice invoice : invoices) {
            if (!"APPOINTMENT".equalsIgnoreCase(invoice.getInvoiceSource())
                    || "CANCELLED".equalsIgnoreCase(invoice.getStatus()) || "DRAFT".equalsIgnoreCase(invoice.getStatus())
                    || "REFUNDED".equalsIgnoreCase(invoice.getPaymentStatus())) continue;
            if (invoice.getPaymentId() != null && processedPaymentIds.contains(invoice.getPaymentId())) continue;

            BigDecimal paid = invoice.getPaidAmount() == null ? BigDecimal.ZERO : invoice.getPaidAmount().max(BigDecimal.ZERO);
            BigDecimal balance = invoice.getBalance() == null ? BigDecimal.ZERO : invoice.getBalance().max(BigDecimal.ZERO);
            total = total.add(paid); pending = pending.add(balance);
            if (invoice.getIssueDate() != null) {
                YearMonth month = YearMonth.from(invoice.getIssueDate());
                buckets.merge(month, paid, BigDecimal::add);
                if (month.equals(current)) monthly = monthly.add(paid);
            }
            if (paid.signum() > 0) {
                rows.add(new PaymentRow(invoice.getId(), invoice.getPatientName() != null ? invoice.getPatientName() : "Patient",
                        invoice.getIssueDate() == null ? "" : invoice.getIssueDate().toLocalDate().toString(),
                        "Consultation", paid, "Paid"));
            }
        }

        // Sort payment rows by date descending
        rows.sort(Comparator.comparing(PaymentRow::date, Comparator.nullsLast(Comparator.reverseOrder())));

        // Build 6-month revenue timeline
        List<Revenue> revenue = new ArrayList<>();
        for (int offset = 5; offset >= 0; offset--) {
            YearMonth month = current.minusMonths(offset);
            revenue.add(new Revenue(month.toString(), buckets.getOrDefault(month, BigDecimal.ZERO)));
        }

        long completed = appointments.stream().filter(a -> a.getStatus() == AppointmentStatus.COMPLETED).count();
        long todays = appointments.stream().filter(a -> today.equals(a.getAppointmentDate()))
                .filter(a -> a.getStatus() != null && (a.getStatus().isActive() || a.getStatus() == AppointmentStatus.COMPLETED)).count();

        return new Earnings(total, monthly, total, pending, completed, todays, revenue, rows);
    }
}
