package lk.gamage.backend.healthbridgebackend.analytics.service.impl;

import lk.gamage.backend.healthbridgebackend.analytics.dto.response.AnalyticsDashboardResponse;
import lk.gamage.backend.healthbridgebackend.analytics.dto.response.AnalyticsKpiResponse;
import lk.gamage.backend.healthbridgebackend.analytics.dto.response.DepartmentPerformanceResponse;
import lk.gamage.backend.healthbridgebackend.analytics.dto.response.DataAvailability;
import lk.gamage.backend.healthbridgebackend.analytics.dto.response.OperationalSummaryResponse;
import lk.gamage.backend.healthbridgebackend.analytics.dto.response.PatientTrendResponse;
import lk.gamage.backend.healthbridgebackend.analytics.dto.response.ResourceUtilizationResponse;
import lk.gamage.backend.healthbridgebackend.analytics.dto.response.RevenueTrendResponse;
import lk.gamage.backend.healthbridgebackend.analytics.service.AnalyticsService;
import lk.gamage.backend.healthbridgebackend.model.LabTest;
import lk.gamage.backend.healthbridgebackend.repository.AppointmentRepository;
import lk.gamage.backend.healthbridgebackend.repository.ConsultationSessionRepository;
import lk.gamage.backend.healthbridgebackend.repository.InsuranceClaimRepository;
import lk.gamage.backend.healthbridgebackend.repository.LabTestRepository;
import lk.gamage.backend.healthbridgebackend.repository.PaymentRepository;
import lk.gamage.backend.healthbridgebackend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.LongSupplier;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsServiceImpl.class);
    private static final String PATIENT_ROLE = "PATIENT";
    private static final Set<String> SUPPORTED_PERIODS = Set.of("today", "week", "month", "year");

    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final ConsultationSessionRepository consultationSessionRepository;
    private final PaymentRepository paymentRepository;
    private final LabTestRepository labTestRepository;
    private final InsuranceClaimRepository insuranceClaimRepository;

    public AnalyticsServiceImpl(
            UserRepository userRepository,
            AppointmentRepository appointmentRepository,
            ConsultationSessionRepository consultationSessionRepository,
            PaymentRepository paymentRepository,
            LabTestRepository labTestRepository,
            InsuranceClaimRepository insuranceClaimRepository
    ) {
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
        this.consultationSessionRepository = consultationSessionRepository;
        this.paymentRepository = paymentRepository;
        this.labTestRepository = labTestRepository;
        this.insuranceClaimRepository = insuranceClaimRepository;
    }

    @Override
    public AnalyticsDashboardResponse getDashboard(String requestedPeriod) {
        String period = normalizePeriod(requestedPeriod);
        LocalDateTime now = LocalDateTime.now();
        PeriodWindow window = periodWindow(period, now);
        RetrievalStatus retrievalStatus = new RetrievalStatus();

        List<AnalyticsKpiResponse> kpis = kpis(window, retrievalStatus);
        List<PatientTrendResponse> patientTrends = patientTrends(period, window, retrievalStatus);
        List<RevenueTrendResponse> revenueTrend = revenueTrend(period, window, retrievalStatus);
        DepartmentAnalytics departmentAnalytics = departmentAnalytics(window, retrievalStatus);

        return new AnalyticsDashboardResponse(
                Instant.now(),
                period,
                retrievalStatus.availability(),
                kpis,
                patientTrends,
                revenueTrend,
                resourceUtilization(),
                departmentAnalytics.performance(),
                departmentAnalytics.operationalSummary()
        );
    }

    private String normalizePeriod(String requestedPeriod) {
        String period = requestedPeriod == null ? "month" : requestedPeriod.trim().toLowerCase(Locale.ROOT);
        if (!SUPPORTED_PERIODS.contains(period)) {
            throw new IllegalArgumentException(
                    "Invalid period '" + requestedPeriod + "'. Supported values: today, week, month, year"
            );
        }
        return period;
    }

    private List<AnalyticsKpiResponse> kpis(PeriodWindow window, RetrievalStatus retrievalStatus) {
        long patients = readCount(
                "patient count",
                () -> userRepository.countByRole(PATIENT_ROLE),
                retrievalStatus);
        long appointments = readCount(
                "appointment count",
                () -> appointmentRepository.countByAppointmentDateGreaterThanEqualAndAppointmentDateLessThan(
                        window.start().toLocalDate(), window.end().toLocalDate().plusDays(1)),
                retrievalStatus);
        long consultations = readCount(
                "consultation count",
                () -> consultationSessionRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                        window.start(), window.end()),
                retrievalStatus);
        long revenue = readRevenue(window.start(), window.end(), retrievalStatus);
        long labTests = readCount(
                "lab-test count",
                () -> labTestRepository.countByRequestedAtGreaterThanEqualAndRequestedAtLessThanAndStatusNot(
                        window.start(), window.end(), LabTest.TestStatus.CANCELLED),
                retrievalStatus);
        long insuranceClaims = readCount(
                "insurance-claim count",
                () -> insuranceClaimRepository.countBySubmittedAtGreaterThanEqualAndSubmittedAtLessThan(
                        window.start(), window.end()),
                retrievalStatus);

        return List.of(
                new AnalyticsKpiResponse("Total Patients", patients),
                new AnalyticsKpiResponse("Total Appointments", appointments),
                new AnalyticsKpiResponse("Total Consultations", consultations),
                new AnalyticsKpiResponse("Total Revenue", revenue),
                new AnalyticsKpiResponse("Lab Tests", labTests),
                new AnalyticsKpiResponse("Insurance Claims", insuranceClaims)
        );
    }

    private List<PatientTrendResponse> patientTrends(
            String period, PeriodWindow window, RetrievalStatus retrievalStatus) {
        DateTimeFormatter formatter = trendLabelFormatter(period);
        List<PatientTrendResponse> trends = new ArrayList<>();
        try {
            for (TimeBucket bucket : trendBuckets(period, window)) {
                long count = userRepository.countByRoleAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                        PATIENT_ROLE, bucket.start(), bucket.end());
                trends.add(new PatientTrendResponse(formatter.format(bucket.start()), count));
            }
            retrievalStatus.recordSuccess();
            return List.copyOf(trends);
        } catch (DataAccessException exception) {
            retrievalStatus.recordFailure();
            log.warn("Analytics patient trends could not be retrieved from MongoDB", exception);
            return List.of();
        }
    }

    private List<RevenueTrendResponse> revenueTrend(
            String period, PeriodWindow window, RetrievalStatus retrievalStatus) {
        DateTimeFormatter formatter = trendLabelFormatter(period);
        List<RevenueTrendResponse> trends = new ArrayList<>();
        try {
            for (TimeBucket bucket : trendBuckets(period, window)) {
                BigDecimal total = confirmedRevenue(bucket.start(), bucket.end());
                trends.add(new RevenueTrendResponse(
                        formatter.format(bucket.start()),
                        total.setScale(0, RoundingMode.HALF_UP).longValue()));
            }
            retrievalStatus.recordSuccess();
            return List.copyOf(trends);
        } catch (DataAccessException exception) {
            retrievalStatus.recordFailure();
            log.warn("Analytics revenue trends could not be retrieved from MongoDB", exception);
            return List.of();
        }
    }

    private long readCount(String metric, LongSupplier query, RetrievalStatus retrievalStatus) {
        try {
            long result = query.getAsLong();
            retrievalStatus.recordSuccess();
            return result;
        } catch (DataAccessException exception) {
            retrievalStatus.recordFailure();
            log.warn("Analytics {} could not be retrieved from MongoDB", metric, exception);
            return 0;
        }
    }

    private long readRevenue(
            LocalDateTime start, LocalDateTime end, RetrievalStatus retrievalStatus) {
        try {
            BigDecimal total = confirmedRevenue(start, end);
            retrievalStatus.recordSuccess();
            return total.setScale(0, RoundingMode.HALF_UP).longValue();
        } catch (DataAccessException exception) {
            retrievalStatus.recordFailure();
            log.warn("Analytics revenue could not be retrieved from MongoDB", exception);
            return 0;
        }
    }

    private BigDecimal confirmedRevenue(LocalDateTime start, LocalDateTime end) {
        return paymentRepository.aggregateConfirmedRevenue(start, end).stream()
                .map(PaymentRepository.ConfirmedRevenue::total)
                .filter(total -> total != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<ResourceUtilizationResponse> resourceUtilization() {
        // None of the dashboard's supplied models record resource capacity or availability.
        return List.of();
    }

    private DepartmentAnalytics departmentAnalytics(
            PeriodWindow window, RetrievalStatus retrievalStatus) {
        try {
            LocalDate start = window.start().toLocalDate();
            LocalDate end = window.end().toLocalDate().plusDays(1);
            Map<String, DepartmentAccumulator> departments = new TreeMap<>();
            for (AppointmentRepository.DepartmentAppointmentStats row
                    : appointmentRepository.aggregateDepartmentAppointments(start, end)) {
                if (row.department() == null || row.department().isBlank()) {
                    continue;
                }
                DepartmentAccumulator department = departments.computeIfAbsent(
                        row.department(), ignored -> new DepartmentAccumulator());
                department.patientIds.addAll(row.patientIds());
                department.appointments += row.appointmentCount();
                department.completedAppointments += row.completedCount();
                if (row.status() != null) {
                    department.statusCounts.merge(row.status(), row.appointmentCount(), Long::sum);
                }
            }

            List<DepartmentPerformanceResponse> performance = new ArrayList<>();
            List<OperationalSummaryResponse> operationalSummary = new ArrayList<>();
            departments.forEach((name, values) -> {
                int completionRate = values.appointments == 0
                        ? 0
                        : (int) Math.round(values.completedAppointments * 100.0 / values.appointments);
                performance.add(new DepartmentPerformanceResponse(name, completionRate));
                operationalSummary.add(new OperationalSummaryResponse(
                        name,
                        values.patientIds.size(),
                        values.appointments,
                        null,
                        null,
                        dominantStatus(values.statusCounts)
                ));
            });
            retrievalStatus.recordSuccess();
            return new DepartmentAnalytics(List.copyOf(performance), List.copyOf(operationalSummary));
        } catch (DataAccessException exception) {
            retrievalStatus.recordFailure();
            log.warn("Analytics department appointment data could not be retrieved from MongoDB", exception);
            return new DepartmentAnalytics(List.of(), List.of());
        }
    }

    private String dominantStatus(Map<String, Long> statusCounts) {
        return statusCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    private PeriodWindow periodWindow(String period, LocalDateTime now) {
        LocalDate today = now.toLocalDate();
        LocalDate startDate = switch (period) {
            case "today" -> today;
            case "week" -> today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
            case "year" -> today.withDayOfYear(1);
            default -> today.withDayOfMonth(1);
        };
        return new PeriodWindow(startDate.atStartOfDay(), now);
    }

    private List<TimeBucket> trendBuckets(String period, PeriodWindow window) {
        List<TimeBucket> buckets = new ArrayList<>();
        LocalDateTime start = window.start();
        while (start.isBefore(window.end())) {
            LocalDateTime next = switch (period) {
                case "today" -> start.plusHours(4);
                case "week" -> start.plusDays(1);
                case "month" -> start.plusWeeks(1);
                case "year" -> start.plusMonths(1);
                default -> throw new IllegalArgumentException("Unsupported analytics period: " + period);
            };
            LocalDateTime end = next.isBefore(window.end()) ? next : window.end();
            buckets.add(new TimeBucket(start, end));
            start = next;
        }
        return buckets;
    }

    private DateTimeFormatter trendLabelFormatter(String period) {
        return switch (period) {
            case "today" -> DateTimeFormatter.ofPattern("HH:mm", Locale.ROOT);
            case "week" -> DateTimeFormatter.ofPattern("EEE", Locale.ROOT);
            case "year" -> DateTimeFormatter.ofPattern("MMM", Locale.ROOT);
            default -> DateTimeFormatter.ofPattern("d MMM", Locale.ROOT);
        };
    }

    private record PeriodWindow(LocalDateTime start, LocalDateTime end) {
    }

    private record TimeBucket(LocalDateTime start, LocalDateTime end) {
    }

    private record DepartmentAnalytics(
            List<DepartmentPerformanceResponse> performance,
            List<OperationalSummaryResponse> operationalSummary
    ) {
    }

    private static final class DepartmentAccumulator {
        private final Set<String> patientIds = new HashSet<>();
        private final Map<String, Long> statusCounts = new HashMap<>();
        private long appointments;
        private long completedAppointments;
    }

    private static final class RetrievalStatus {
        private int successfulReads;
        private int failedReads;

        private void recordSuccess() {
            successfulReads++;
        }

        private void recordFailure() {
            failedReads++;
        }

        private DataAvailability availability() {
            if (successfulReads == 0 && failedReads > 0) {
                return DataAvailability.UNAVAILABLE;
            }
            return DataAvailability.PARTIAL;
        }
    }
}
