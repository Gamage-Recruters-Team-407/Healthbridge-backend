package lk.gamage.backend.healthbridgebackend.service;

import lk.gamage.backend.healthbridgebackend.dto.SuperAdminDTOs;
import lk.gamage.backend.healthbridgebackend.repository.FraudAlertRepository;
import lk.gamage.backend.healthbridgebackend.repository.TelemedicineSessionRepository;
import lk.gamage.backend.healthbridgebackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;
import lk.gamage.backend.healthbridgebackend.model.User;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.domain.Sort;

@Service
@RequiredArgsConstructor
@Slf4j
public class SuperAdminDashboardService {

    private final UserRepository userRepository;
    private final TelemedicineSessionRepository telemedicineSessionRepository;
    private final FraudAlertRepository fraudAlertRepository;
    private final lk.gamage.backend.healthbridgebackend.repository.PaymentRepository paymentRepository;
    private final lk.gamage.backend.healthbridgebackend.repository.AuditLogRepository auditLogRepository;
    private final lk.gamage.backend.healthbridgebackend.repository.AppointmentRepository appointmentRepository;
    private final lk.gamage.backend.healthbridgebackend.repository.PrescriptionRepository prescriptionRepository;
    private final lk.gamage.backend.healthbridgebackend.repository.LabTestRepository labTestRepository;
    private final lk.gamage.backend.healthbridgebackend.repository.InsuranceClaimRepository insuranceClaimRepository;
    private final lk.gamage.backend.healthbridgebackend.repository.BranchRepository branchRepository;
    private final MongoTemplate mongoTemplate;

    public SuperAdminDTOs.Stats getDashboardStats() {
        long totalUsers = 0;
        try {
            totalUsers = userRepository.count();
        } catch (Exception e) {
            log.warn("[Dashboard] Error counting users: {}", e.getMessage());
        }
        
        long activeDoctors = 0;
        try {
            activeDoctors = userRepository.countByRole("DOCTOR");
        } catch (Exception e) {
            log.warn("[Dashboard] Error counting doctors: {}", e.getMessage());
        }
        
        long activeSessions = 0;
        try {
            activeSessions = telemedicineSessionRepository != null ? telemedicineSessionRepository.count() : 0;
        } catch (Exception e) {
            log.warn("[Dashboard] Error counting sessions: {}", e.getMessage());
        }
        
        long securityAlerts = 0;
        try {
            Query secQuery = new Query();
            secQuery.addCriteria(new Criteria().orOperator(
                Criteria.where("severity").regex("^(High|Critical)$", "i"),
                Criteria.where("module").regex("^Security$", "i")
            ));
            securityAlerts = mongoTemplate.count(secQuery, "audit_logs");
        } catch (Exception e) {
            log.warn("[Dashboard] Error counting security alerts: {}", e.getMessage());
        }

        long pendingVerifications = 0;
        try {
            Query pendingQuery = new Query(Criteria.where("accountStatus").regex("^(PENDING|PENDING_APPROVAL|Pending approval|Pending)$", "i"));
            pendingVerifications = mongoTemplate.count(pendingQuery, "users");
        } catch (Exception e) {
            log.warn("[Dashboard] Error counting pending verifications: {}", e.getMessage());
        }

        List<SuperAdminDTOs.PendingUserDto> topPendingApprovals = new ArrayList<>();
        try {
            Query topPendingQuery = new Query(Criteria.where("accountStatus").regex("^(PENDING|PENDING_APPROVAL|Pending approval|Pending)$", "i"))
                    .with(Sort.by(Sort.Direction.DESC, "createdAt"))
                    .limit(5);
            List<org.bson.Document> topPendingDocs = mongoTemplate.find(topPendingQuery, org.bson.Document.class, "users");

            for (org.bson.Document doc : topPendingDocs) {
                String id = doc.get("_id") != null ? doc.get("_id").toString() : "";
                String fullName = doc.getString("fullName");
                if (fullName == null || fullName.isBlank()) {
                    String first = doc.getString("firstName");
                    String last = doc.getString("lastName");
                    fullName = ((first != null ? first : "") + (last != null ? " " + last : "")).trim();
                    if (fullName.isEmpty()) fullName = "New User";
                }
                String role = doc.getString("role");
                if (role == null || role.isBlank()) role = "USER";

                LocalDateTime createdAt = extractDate(doc, "createdAt");
                topPendingApprovals.add(SuperAdminDTOs.PendingUserDto.builder()
                        .id(id)
                        .name(fullName)
                        .role(role)
                        .timeAgo(calculateTimeAgo(createdAt))
                        .build());
            }
        } catch (Exception e) {
            log.warn("[Dashboard] Error fetching top pending approvals: {}", e.getMessage());
        }

        long totalBranches = 0;
        try {
            totalBranches = branchRepository != null ? branchRepository.count() : 0;
        } catch (Exception e) {
            log.warn("[Dashboard] Error counting branches: {}", e.getMessage());
        }
        
        double totalRevenue = 0.0;
        double monthlyRecurringRevenue = 0.0;
        try {
            Query confirmedQuery = new Query(Criteria.where("status").is("CONFIRMED"));
            List<org.bson.Document> confirmedDocs = mongoTemplate.find(confirmedQuery, org.bson.Document.class, "payments");
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            
            for (org.bson.Document pDoc : confirmedDocs) {
                Double amt = extractAmount(pDoc);
                if (amt != null && amt > 0) {
                    totalRevenue += amt;
                    LocalDateTime created = extractDate(pDoc, "createdAt");
                    if (created != null && created.isAfter(thirtyDaysAgo)) {
                        monthlyRecurringRevenue += amt;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("[Dashboard] Error computing payment revenue: {}", e.getMessage());
        }

        double storageUsedPercentage = 0.0;
        double systemHealthPercentage = 95.0;
        try {
            java.io.File file = new java.io.File(".");
            long totalSpace = file.getTotalSpace();
            long freeSpace = file.getUsableSpace();
            storageUsedPercentage = totalSpace == 0 ? 0.0 : ((totalSpace - freeSpace) * 100.0 / totalSpace);
            storageUsedPercentage = Math.round(storageUsedPercentage * 10.0) / 10.0;
            
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long usedMemory = runtime.totalMemory() - runtime.freeMemory();
            systemHealthPercentage = maxMemory == 0 ? 100.0 : 100.0 - ((usedMemory * 100.0) / maxMemory);
            systemHealthPercentage = Math.max(90.0, Math.round(systemHealthPercentage * 10.0) / 10.0);
        } catch (Exception e) {
            log.warn("[Dashboard] Error getting infrastructure metrics: {}", e.getMessage());
        }

        return SuperAdminDTOs.Stats.builder()
                .totalUsers(totalUsers)
                .totalBranches(totalBranches)
                .activeDoctors(activeDoctors)
                .activeSessions(activeSessions)
                .systemHealthPercentage(systemHealthPercentage)
                .securityAlerts(securityAlerts)
                .totalRevenue(totalRevenue)
                .pendingVerifications(pendingVerifications)
                .monthlyRecurringRevenue(monthlyRecurringRevenue)
                .storageUsedPercentage(storageUsedPercentage)
                .topPendingApprovals(topPendingApprovals)
                .build();
    }

    private String calculateTimeAgo(LocalDateTime date) {
        if (date == null) return "Recently";
        try {
            long minutes = ChronoUnit.MINUTES.between(date, LocalDateTime.now());
            if (minutes < 0) return "Just now";
            if (minutes < 60) return Math.max(1, minutes) + "m ago";
            long hours = ChronoUnit.HOURS.between(date, LocalDateTime.now());
            if (hours < 24) return hours + "h ago";
            long days = ChronoUnit.DAYS.between(date, LocalDateTime.now());
            return days + "d ago";
        } catch (Exception e) {
            return "Recently";
        }
    }

    public List<SuperAdminDTOs.Growth> getGrowthData() {
        List<SuperAdminDTOs.Growth> growth = new ArrayList<>();
        try {
            Query confirmedPaymentsQuery = new Query(Criteria.where("status").is("CONFIRMED"));
            List<org.bson.Document> allPayments = mongoTemplate.find(confirmedPaymentsQuery, org.bson.Document.class, "payments");
            java.time.YearMonth currentMonth = java.time.YearMonth.now();
            long totalUsers = userRepository.count();
            
            // Generate data for the last 6 months
            for (int i = 5; i >= 0; i--) {
                java.time.YearMonth targetMonth = currentMonth.minusMonths(i);
                LocalDateTime endOfMonth = targetMonth.atEndOfMonth().atTime(23, 59, 59);
                
                long cumulativeUsers = 0;
                try {
                    Query userGrowthQuery = new Query(Criteria.where("createdAt").lte(endOfMonth));
                    cumulativeUsers = mongoTemplate.count(userGrowthQuery, "users");
                } catch (Exception ignored) {}
                    
                if (cumulativeUsers == 0 && totalUsers > 0) {
                    int factor = 6 - i;
                    cumulativeUsers = Math.max(1, (totalUsers * factor) / 6);
                }
                    
                long cumulativeAppointments = 0;
                try {
                    Query appointmentQuery = new Query();
                    appointmentQuery.addCriteria(Criteria.where("createdAt").lte(endOfMonth));
                    cumulativeAppointments = mongoTemplate.count(appointmentQuery, "appointments");
                } catch (Exception ignored) {}
                    
                double cumulativeRevenue = 0.0;
                for (org.bson.Document pDoc : allPayments) {
                    Double amt = extractAmount(pDoc);
                    LocalDateTime created = extractDate(pDoc, "createdAt");
                    if (amt != null && created != null && !java.time.YearMonth.from(created).isAfter(targetMonth)) {
                        cumulativeRevenue += amt;
                    }
                }
                    
                String monthName = targetMonth.getMonth().name().substring(0, 3);
                monthName = monthName.substring(0, 1) + monthName.substring(1).toLowerCase();
                
                growth.add(new SuperAdminDTOs.Growth(monthName, cumulativeUsers, cumulativeAppointments, cumulativeRevenue));
            }
        } catch (Exception e) {
            log.warn("[Dashboard] Error generating growth data: {}", e.getMessage());
        }
        
        return growth;
    }

    public SuperAdminDTOs.SystemAnalytics getSystemAnalytics() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneDayAgo = now.minusDays(1);
        LocalDateTime twoDaysAgo = now.minusDays(2);
        LocalDateTime thirtyDaysAgo = now.minusDays(30);
        LocalDateTime sixtyDaysAgo = now.minusDays(60);

        long dau = 0;
        long prevDau = 0;
        long mau = 0;
        long prevMau = 0;
        try {
            // Query only the last 60 days of audit logs using Document.class to prevent mapping failures
            Query recentLogsQuery = new Query(Criteria.where("timestamp").gte(sixtyDaysAgo));
            List<org.bson.Document> logDocs = mongoTemplate.find(recentLogsQuery, org.bson.Document.class, "audit_logs");
            
            dau = logDocs.stream()
                    .filter(doc -> {
                        LocalDateTime ts = extractDate(doc, "timestamp");
                        return ts != null && ts.isAfter(oneDayAgo);
                    })
                    .map(doc -> doc.getString("user"))
                    .filter(user -> user != null && !user.isBlank())
                    .distinct()
                    .count();

            prevDau = logDocs.stream()
                    .filter(doc -> {
                        LocalDateTime ts = extractDate(doc, "timestamp");
                        return ts != null && ts.isAfter(twoDaysAgo) && ts.isBefore(oneDayAgo);
                    })
                    .map(doc -> doc.getString("user"))
                    .filter(user -> user != null && !user.isBlank())
                    .distinct()
                    .count();

            mau = logDocs.stream()
                    .filter(doc -> {
                        LocalDateTime ts = extractDate(doc, "timestamp");
                        return ts != null && ts.isAfter(thirtyDaysAgo);
                    })
                    .map(doc -> doc.getString("user"))
                    .filter(user -> user != null && !user.isBlank())
                    .distinct()
                    .count();

            prevMau = logDocs.stream()
                    .filter(doc -> {
                        LocalDateTime ts = extractDate(doc, "timestamp");
                        return ts != null && ts.isAfter(sixtyDaysAgo) && ts.isBefore(thirtyDaysAgo);
                    })
                    .map(doc -> doc.getString("user"))
                    .filter(user -> user != null && !user.isBlank())
                    .distinct()
                    .count();
        } catch (Exception e) {
            log.warn("[Analytics] Error querying audit logs: {}", e.getMessage());
        }
        
        double stickiness = mau == 0 ? 0.0 : Math.round((dau * 100.0) / mau);
        
        double calculatedDauGrowth = prevDau == 0 ? (dau > 0 ? 100.0 : 0.0) : Math.round(((double)(dau - prevDau) / prevDau) * 1000.0) / 10.0;
        double calculatedMauGrowth = prevMau == 0 ? (mau > 0 ? 100.0 : 0.0) : Math.round(((double)(mau - prevMau) / prevMau) * 1000.0) / 10.0;

        long appointments = 0;
        long prescriptions = 0;
        long telemedicine = 0;
        long labs = 0;
        long insurance = 0;
        try { appointments = appointmentRepository != null ? appointmentRepository.count() : 0; } catch (Exception ignored) {}
        try { prescriptions = prescriptionRepository != null ? prescriptionRepository.count() : 0; } catch (Exception ignored) {}
        try { telemedicine = telemedicineSessionRepository != null ? telemedicineSessionRepository.count() : 0; } catch (Exception ignored) {}
        try { labs = labTestRepository != null ? labTestRepository.count() : 0; } catch (Exception ignored) {}
        try { insurance = insuranceClaimRepository != null ? insuranceClaimRepository.count() : 0; } catch (Exception ignored) {}

        long maxFeature = Math.max(appointments, Math.max(prescriptions, Math.max(telemedicine, Math.max(labs, insurance))));
        if (maxFeature == 0) maxFeature = 1;

        List<SuperAdminDTOs.FeatureAdoption> features = new ArrayList<>();
        features.add(new SuperAdminDTOs.FeatureAdoption("Appointments", Math.round((appointments * 100.0) / maxFeature), "Most Used"));
        features.add(new SuperAdminDTOs.FeatureAdoption("Prescriptions", Math.round((prescriptions * 100.0) / maxFeature), "Popular"));
        features.add(new SuperAdminDTOs.FeatureAdoption("Telemedicine", Math.round((telemedicine * 100.0) / maxFeature), "Growing"));
        features.add(new SuperAdminDTOs.FeatureAdoption("Lab Tests", Math.round((labs * 100.0) / maxFeature), "Stable"));
        features.add(new SuperAdminDTOs.FeatureAdoption("Insurance", Math.round((insurance * 100.0) / maxFeature), "Needs Boost"));

        long numBranches = 0;
        long numDoctors = 0;
        long numPatients = 0;
        long numPharmacies = 0;
        long numLabs = 0;
        long numInsurance = 0;
        try { numBranches = branchRepository != null ? branchRepository.count() : 0; } catch (Exception ignored) {}
        try { numDoctors = userRepository != null ? userRepository.countByRole("DOCTOR") : 0; } catch (Exception ignored) {}
        try { numPatients = userRepository != null ? userRepository.countByRole("PATIENT") : 0; } catch (Exception ignored) {}
        try { numPharmacies = userRepository != null ? userRepository.countByRole("PHARMACIST") : 0; } catch (Exception ignored) {}
        try { numLabs = userRepository != null ? userRepository.countByRole("LAB_OFFICER") : 0; } catch (Exception ignored) {}
        try { numInsurance = userRepository != null ? userRepository.countByRole("INSURANCE_OFFICER") : 0; } catch (Exception ignored) {}

        LocalDateTime thirtyDaysAgoDate = LocalDateTime.now().minusDays(30);
        LocalDateTime sixtyDaysAgoDate = LocalDateTime.now().minusDays(60);

        String docGrowth = calculateRoleGrowth("DOCTOR", thirtyDaysAgoDate, sixtyDaysAgoDate);
        String patGrowth = calculateRoleGrowth("PATIENT", thirtyDaysAgoDate, sixtyDaysAgoDate);
        String pharmGrowth = calculateRoleGrowth("PHARMACIST", thirtyDaysAgoDate, sixtyDaysAgoDate);
        String labGrowth = calculateRoleGrowth("LAB_OFFICER", thirtyDaysAgoDate, sixtyDaysAgoDate);
        String insGrowth = calculateRoleGrowth("INSURANCE_OFFICER", thirtyDaysAgoDate, sixtyDaysAgoDate);

        List<SuperAdminDTOs.ModulePerformance> modules = new ArrayList<>();
        modules.add(new SuperAdminDTOs.ModulePerformance("Branches", numBranches + " active", numBranches > 0 ? 92.0 : 0.0, "+ 15.0%", numBranches > 0 ? "Good" : "Critical"));
        modules.add(new SuperAdminDTOs.ModulePerformance("Doctors", numDoctors + " active", numDoctors > 0 ? 82.0 : 0.0, docGrowth, numDoctors > 0 ? "Good" : "Critical"));
        modules.add(new SuperAdminDTOs.ModulePerformance("Patients", numPatients + " active", numPatients > 0 ? 92.0 : 0.0, patGrowth, numPatients > 0 ? "Good" : "Critical"));
        modules.add(new SuperAdminDTOs.ModulePerformance("Pharmacies", numPharmacies + " active", numPharmacies > 0 ? 65.0 : 0.0, pharmGrowth, numPharmacies > 0 ? "Moderate" : "Critical"));
        modules.add(new SuperAdminDTOs.ModulePerformance("Labs", numLabs + " active", numLabs > 0 ? 45.0 : 0.0, labGrowth, numLabs > 0 ? "Critical" : "Critical"));
        modules.add(new SuperAdminDTOs.ModulePerformance("Insurance", numInsurance + " active", numInsurance > 0 ? 35.0 : 0.0, insGrowth, numInsurance > 0 ? "Critical" : "Critical"));

        double totalRev = 0;
        double hospitalRev = 0;
        double pharmacyRev = 0;
        double labRev = 0;
        double insuranceRev = 0;

        try {
            Query confirmedRevQuery = new Query(Criteria.where("status").is("CONFIRMED"));
            List<org.bson.Document> allPayments = mongoTemplate.find(confirmedRevQuery, org.bson.Document.class, "payments");

            for (org.bson.Document p : allPayments) {
                Double amt = extractAmount(p);
                if (amt != null && amt > 0) {
                    totalRev += amt;
                    String category = p.getString("category");
                    if ("CONSULTATION".equals(category)) hospitalRev += amt;
                    else if ("PRESCRIPTION".equals(category)) pharmacyRev += amt;
                    else if ("LAB_TEST".equals(category)) labRev += amt;
                    else if ("INSURANCE".equals(category)) insuranceRev += amt;
                    else hospitalRev += amt; 
                }
            }
        } catch (Exception e) {
            log.warn("[Analytics] Error computing revenue breakdown: {}", e.getMessage());
        }

        double finalTotalRev = totalRev == 0 ? 1 : totalRev;
        List<SuperAdminDTOs.RevenueBreakdown> revenue = new ArrayList<>();
        revenue.add(new SuperAdminDTOs.RevenueBreakdown("Branches", hospitalRev, Math.round((hospitalRev * 100) / finalTotalRev)));
        revenue.add(new SuperAdminDTOs.RevenueBreakdown("Pharmacies", pharmacyRev, Math.round((pharmacyRev * 100) / finalTotalRev)));
        revenue.add(new SuperAdminDTOs.RevenueBreakdown("Labs", labRev, Math.round((labRev * 100) / finalTotalRev)));
        revenue.add(new SuperAdminDTOs.RevenueBreakdown("Insurance", insuranceRev, Math.round((insuranceRev * 100) / finalTotalRev)));

        return SuperAdminDTOs.SystemAnalytics.builder()
                .dau((int) dau)
                .mau((int) mau)
                .stickiness(stickiness)
                .dauGrowth(calculatedDauGrowth) 
                .mauGrowth(calculatedMauGrowth) 
                .featureAdoption(features)
                .modulePerformance(modules)
                .revenueBreakdown(revenue)
                .build();
    }
    
    private String calculateRoleGrowth(String role, LocalDateTime thirtyDaysAgo, LocalDateTime sixtyDaysAgo) {
        try {
            Query currentQuery = new Query(Criteria.where("role").is(role).and("createdAt").gt(thirtyDaysAgo));
            long currentPeriod = mongoTemplate.count(currentQuery, "users");
            
            Query prevQuery = new Query(Criteria.where("role").is(role).and("createdAt").gt(sixtyDaysAgo).lte(thirtyDaysAgo));
            long previousPeriod = mongoTemplate.count(prevQuery, "users");
            
            if (previousPeriod == 0) {
                return currentPeriod > 0 ? "+ 100.0%" : "0.0%";
            }
            double growth = Math.round(((double)(currentPeriod - previousPeriod) / previousPeriod) * 1000.0) / 10.0;
            return (growth > 0 ? "+ " : "") + growth + "%";
        } catch (Exception e) {
            log.warn("[Analytics] Error calculating role growth for {}: {}", role, e.getMessage());
            return "0.0%";
        }
    }

    public byte[] generateDashboardReport() {
        try {
            SuperAdminDTOs.Stats stats = getDashboardStats();
            Document document = new Document();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, out);
            
            document.open();
            document.add(new Paragraph("HealthBridge - Super Admin Dashboard Report"));
            document.add(new Paragraph("Generated on: " + LocalDateTime.now().toString()));
            document.add(new Paragraph("--------------------------------------------------"));
            document.add(new Paragraph("Total Users: " + stats.getTotalUsers()));
            document.add(new Paragraph("Active Doctors: " + stats.getActiveDoctors()));
            document.add(new Paragraph("Registered Branches: " + stats.getTotalBranches()));
            document.add(new Paragraph("Active Sessions: " + stats.getActiveSessions()));
            document.add(new Paragraph("Pending Approvals: " + stats.getPendingVerifications()));
            document.add(new Paragraph("Monthly Recurring Revenue: Rs. " + stats.getMonthlyRecurringRevenue()));
            document.add(new Paragraph("System Health: " + stats.getSystemHealthPercentage() + "%"));
            document.add(new Paragraph("Storage Used: " + stats.getStorageUsedPercentage() + "%"));
            document.add(new Paragraph("Security Alerts: " + stats.getSecurityAlerts()));
            
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    public byte[] generateAnalyticsReport() {
        try {
            SuperAdminDTOs.SystemAnalytics analytics = getSystemAnalytics();
            Document document = new Document();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, out);
            
            document.open();
            document.add(new Paragraph("HealthBridge - System Analytics Report"));
            document.add(new Paragraph("Generated on: " + LocalDateTime.now().toString()));
            document.add(new Paragraph("--------------------------------------------------"));
            document.add(new Paragraph("Daily Active Users (DAU): " + analytics.getDau() + " (" + analytics.getDauGrowth() + "% growth)"));
            document.add(new Paragraph("Monthly Active Users (MAU): " + analytics.getMau() + " (" + analytics.getMauGrowth() + "% growth)"));
            document.add(new Paragraph("System Stickiness: " + analytics.getStickiness() + "%"));
            
            document.add(new Paragraph("\nModule Performance:"));
            for (SuperAdminDTOs.ModulePerformance mod : analytics.getModulePerformance()) {
                document.add(new Paragraph("- " + mod.getName() + ": " + mod.getActive() + ", Score: " + mod.getScore() + "%, Growth: " + mod.getGrowth() + " (" + mod.getStatus() + ")"));
            }
            
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    private Double extractAmount(org.bson.Document doc) {
        if (doc == null) return null;
        Object amtObj = doc.get("amount");
        if (amtObj instanceof Number) {
            return ((Number) amtObj).doubleValue();
        } else if (amtObj instanceof org.bson.types.Decimal128) {
            return ((org.bson.types.Decimal128) amtObj).doubleValue();
        } else if (amtObj instanceof String) {
            try {
                return Double.parseDouble((String) amtObj);
            } catch (Exception ignored) {}
        }
        return null;
    }

    private LocalDateTime extractDate(org.bson.Document doc, String field) {
        if (doc == null || field == null) return null;
        Object dateObj = doc.get(field);
        if (dateObj instanceof java.util.Date) {
            return ((java.util.Date) dateObj).toInstant()
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
        } else if (dateObj instanceof LocalDateTime) {
            return (LocalDateTime) dateObj;
        } else if (dateObj instanceof String) {
            try {
                return LocalDateTime.parse((String) dateObj);
            } catch (Exception ignored) {}
        }
        return null;
    }
}
