package lk.gamage.backend.healthbridgebackend.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lk.gamage.backend.healthbridgebackend.model.Appointment;
import lk.gamage.backend.healthbridgebackend.model.Invoice;
import lk.gamage.backend.healthbridgebackend.enums.AppointmentStatus;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DoctorMetricsServiceTest {
    @Test
    void distinguishesTodaysBookingsCompletedConsultationsAndPaidInvoices() {
        LocalDate today = LocalDate.of(2026, 10, 9);
        Invoice current = invoice(today, "4000", "1000");
        Invoice previous = invoice(today.minusMonths(1), "2000", "0");
        Invoice draft = invoice(today, "9000", "0"); draft.setStatus("DRAFT");
        Invoice refunded = invoice(today, "9000", "0"); refunded.setPaymentStatus("REFUNDED");
        Invoice lab = invoice(today, "9000", "0"); lab.setInvoiceSource("LAB");
        var result = DoctorMetricsService.summarize(List.of(
                appointment(today, AppointmentStatus.BOOKED),
                appointment(today, AppointmentStatus.COMPLETED),
                appointment(today, AppointmentStatus.CANCELLED),
                appointment(today.plusDays(1), AppointmentStatus.UPCOMING),
                appointment(today.minusDays(1), AppointmentStatus.BOOKED)),
                List.of(current, previous, draft, refunded, lab), today);
        assertEquals(2, result.todayAppointments());
        assertEquals(1, result.completedConsultations());
        assertEquals(new BigDecimal("6000"), result.totalEarnings());
        assertEquals(new BigDecimal("4000"), result.monthlyEarnings());
        assertEquals(new BigDecimal("1000"), result.pendingAmount());
        assertEquals(2, result.payments().size());
        assertEquals(6, result.revenue().size());
        assertEquals("2026-10", result.revenue().get(5).month());
        assertEquals(new BigDecimal("4000"), result.revenue().get(5).amount());
    }

    @Test
    void unpaidBookingsDoNotCreateEarnings() {
        LocalDate today = LocalDate.of(2026, 10, 9);
        var result = DoctorMetricsService.summarize(
                List.of(appointment(today, AppointmentStatus.BOOKED)),
                List.of(invoice(today, "0", "8000")), today);
        assertEquals(1, result.todayAppointments());
        assertEquals(0, result.completedConsultations());
        assertEquals(BigDecimal.ZERO, result.totalEarnings());
        assertEquals(new BigDecimal("8000"), result.pendingAmount());
        assertTrue(result.payments().isEmpty());
    }

    private static Appointment appointment(LocalDate date, AppointmentStatus status) {
        Appointment appointment = new Appointment();
        appointment.setAppointmentDate(date); appointment.setStatus(status);
        return appointment;
    }
    private static Invoice invoice(LocalDate date, String paid, String balance) {
        Invoice invoice = new Invoice();
        invoice.setInvoiceSource("APPOINTMENT"); invoice.setStatus("ISSUED");
        invoice.setIssueDate(date.atStartOfDay());
        invoice.setPaidAmount(new BigDecimal(paid)); invoice.setBalance(new BigDecimal(balance));
        return invoice;
    }
}
