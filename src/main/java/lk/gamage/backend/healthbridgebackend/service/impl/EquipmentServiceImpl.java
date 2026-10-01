package lk.gamage.backend.healthbridgebackend.service.impl;

import lk.gamage.backend.healthbridgebackend.dto.EquipmentRequestDto;
import lk.gamage.backend.healthbridgebackend.dto.EquipmentResponseDto;
import lk.gamage.backend.healthbridgebackend.dto.EquipmentStatsDto;
import lk.gamage.backend.healthbridgebackend.exception.ResourceNotFoundException;
import lk.gamage.backend.healthbridgebackend.model.Bed;
import lk.gamage.backend.healthbridgebackend.model.Department;
import lk.gamage.backend.healthbridgebackend.model.Equipment;
import lk.gamage.backend.healthbridgebackend.repository.BedRepository;
import lk.gamage.backend.healthbridgebackend.repository.DepartmentRepository;
import lk.gamage.backend.healthbridgebackend.repository.EquipmentRepository;
import lk.gamage.backend.healthbridgebackend.service.EquipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class EquipmentServiceImpl implements EquipmentService {

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private BedRepository bedRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @PostConstruct
    public void seedInitialData() {
        if (equipmentRepository.count() == 0) {
            List<Equipment> seedData = Arrays.asList(
                    Equipment.builder()
                            .assetId("#EQ-10492")
                            .name("Ventilator X200")
                            .category("Life Support")
                            .department("ICU")
                            .location("ICU - Bed 04")
                            .serialNo("SN-9948201")
                            .status("In Use")
                            .calibrationDueDate("Aug 15, 2026")
                            .model("X200")
                            .supplier("MedTech Corp")
                            .purchaseDate("Jan 12, 2024")
                            .warrantyExpiry("Dec 31, 2026")
                            .depreciationPercentage(82)
                            .initialValue(45000.0)
                            .currentValue(36900.0)
                            .alertMessage("Low Stock: O2 Sensors (2 units remaining in supply)")
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build(),
                    Equipment.builder()
                            .assetId("#EQ-06221")
                            .name("Portable Ultrasound")
                            .category("Diagnostic")
                            .department("Radiology")
                            .location("Radiology - Room 2")
                            .serialNo("SN-4410923")
                            .status("Available")
                            .calibrationDueDate("Jul 02, 2026")
                            .model("Ultrasound Pro")
                            .supplier("Philips Healthcare")
                            .purchaseDate("Mar 20, 2023")
                            .warrantyExpiry("Mar 20, 2027")
                            .depreciationPercentage(75)
                            .initialValue(28000.0)
                            .currentValue(21000.0)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build(),
                    Equipment.builder()
                            .assetId("#EQ-05119")
                            .name("Infusion Pump B4")
                            .category("Monitoring")
                            .department("ER")
                            .location("ER - Bay 12")
                            .serialNo("SN-1192847")
                            .status("Maintenance")
                            .calibrationDueDate("Sep 01, 2026")
                            .model("Pump B4")
                            .supplier("Baxter Medical")
                            .purchaseDate("Nov 05, 2022")
                            .warrantyExpiry("Nov 05, 2025")
                            .depreciationPercentage(60)
                            .initialValue(12000.0)
                            .currentValue(7200.0)
                            .alertMessage("Maintenance Required: Battery replacement")
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build(),
                    Equipment.builder()
                            .assetId("#EQ-08331")
                            .name("Defibrillator Pro")
                            .category("Life Support")
                            .department("ER")
                            .location("Storage Room B")
                            .serialNo("SN-7729103")
                            .status("Available")
                            .calibrationDueDate("Nov 11, 2026")
                            .model("Defib Pro 300")
                            .supplier("Zoll Medical")
                            .purchaseDate("Feb 14, 2024")
                            .warrantyExpiry("Feb 14, 2028")
                            .depreciationPercentage(90)
                            .initialValue(18000.0)
                            .currentValue(16200.0)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build(),
                    Equipment.builder()
                            .assetId("#EQ-12005")
                            .name("Infusion Pump Z")
                            .category("Monitoring")
                            .department("Biomed Workshop")
                            .location("Biomed Workshop")
                            .serialNo("SN-3391024")
                            .status("Maintenance")
                            .calibrationDueDate("Oct 10, 2026")
                            .model("Pump Z-100")
                            .supplier("Braun Healthcare")
                            .purchaseDate("Jun 18, 2023")
                            .warrantyExpiry("Jun 18, 2026")
                            .depreciationPercentage(65)
                            .initialValue(9500.0)
                            .currentValue(6175.0)
                            .alertMessage("Est. Return: Tomorrow")
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build(),
                    Equipment.builder()
                            .assetId("#EQ-14920")
                            .name("Anesthesia Workstation")
                            .category("Surgical")
                            .department("Surgery")
                            .location("OR - Suite 3")
                            .serialNo("SN-8829104")
                            .status("In Use")
                            .calibrationDueDate("Dec 05, 2026")
                            .model("Flow-i C30")
                            .supplier("Getinge Group")
                            .purchaseDate("Aug 01, 2023")
                            .warrantyExpiry("Aug 01, 2027")
                            .depreciationPercentage(85)
                            .initialValue(65000.0)
                            .currentValue(55250.0)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build()
            );
            equipmentRepository.saveAll(seedData);
        }
    }

    @Override
    public List<EquipmentResponseDto> getAllEquipment(String category, String department, String status, String search) {
        List<Equipment> list = equipmentRepository.findAll();

        if (category != null && !category.trim().isEmpty() && !"All".equalsIgnoreCase(category)) {
            list = list.stream()
                    .filter(e -> e.getCategory() != null && e.getCategory().equalsIgnoreCase(category.trim()))
                    .collect(Collectors.toList());
        }

        if (department != null && !department.trim().isEmpty() && !"All".equalsIgnoreCase(department)) {
            list = list.stream()
                    .filter(e -> e.getDepartment() != null && e.getDepartment().equalsIgnoreCase(department.trim()))
                    .collect(Collectors.toList());
        }

        if (status != null && !status.trim().isEmpty() && !"All".equalsIgnoreCase(status)) {
            list = list.stream()
                    .filter(e -> e.getStatus() != null && e.getStatus().equalsIgnoreCase(status.trim()))
                    .collect(Collectors.toList());
        }

        if (search != null && !search.trim().isEmpty()) {
            String lowerSearch = search.trim().toLowerCase();
            list = list.stream()
                    .filter(e -> (e.getName() != null && e.getName().toLowerCase().contains(lowerSearch)) ||
                            (e.getAssetId() != null && e.getAssetId().toLowerCase().contains(lowerSearch)) ||
                            (e.getSerialNo() != null && e.getSerialNo().toLowerCase().contains(lowerSearch)) ||
                            (e.getDepartment() != null && e.getDepartment().toLowerCase().contains(lowerSearch)) ||
                            (e.getCategory() != null && e.getCategory().toLowerCase().contains(lowerSearch)))
                    .collect(Collectors.toList());
        }

        return list.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public EquipmentResponseDto getEquipmentById(String id) {
        Equipment equipment = findEntity(id);
        return mapToDto(equipment);
    }

    @Override
    public EquipmentResponseDto createEquipment(EquipmentRequestDto request) {
        String assetId = request.getAssetId();
        if (assetId == null || assetId.trim().isEmpty() || assetId.toLowerCase().contains("auto")) {
            int num = 10000 + new Random().nextInt(90000);
            assetId = "#EQ-" + num;
            while (equipmentRepository.existsByAssetId(assetId)) {
                num = 10000 + new Random().nextInt(90000);
                assetId = "#EQ-" + num;
            }
        }

        Equipment equipment = Equipment.builder()
                .assetId(assetId.trim())
                .name(request.getName() != null ? request.getName().trim() : "New Equipment")
                .category(request.getCategory() != null ? request.getCategory().trim() : "General")
                .department(request.getDepartment() != null ? request.getDepartment().trim() : "ICU")
                .location(request.getLocation() != null ? request.getLocation().trim() : "Storage")
                .serialNo(request.getSerialNo() != null ? request.getSerialNo().trim() : "SN-" + System.currentTimeMillis())
                .status(request.getStatus() != null ? request.getStatus().trim() : "Available")
                .calibrationDueDate(request.getCalibrationDueDate() != null ? request.getCalibrationDueDate().trim() : "Aug 15, 2026")
                .model(request.getModel() != null ? request.getModel().trim() : "Standard")
                .supplier(request.getSupplier() != null ? request.getSupplier().trim() : "MedTech Corp")
                .purchaseDate(request.getPurchaseDate() != null ? request.getPurchaseDate().trim() : "2024-01-12")
                .warrantyExpiry(request.getWarrantyExpiry() != null ? request.getWarrantyExpiry().trim() : "2026-12-31")
                .depreciationPercentage(request.getDepreciationPercentage() != null ? request.getDepreciationPercentage() : 85)
                .initialValue(request.getInitialValue() != null ? request.getInitialValue() : 25000.0)
                .currentValue(request.getCurrentValue() != null ? request.getCurrentValue() : 21250.0)
                .alertMessage(request.getAlertMessage())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Equipment saved = equipmentRepository.save(equipment);
        return mapToDto(saved);
    }

    @Override
    public EquipmentResponseDto updateEquipment(String id, EquipmentRequestDto request) {
        Equipment equipment = findEntity(id);

        if (request.getName() != null) equipment.setName(request.getName().trim());
        if (request.getCategory() != null) equipment.setCategory(request.getCategory().trim());
        if (request.getDepartment() != null) equipment.setDepartment(request.getDepartment().trim());
        if (request.getLocation() != null) equipment.setLocation(request.getLocation().trim());
        if (request.getSerialNo() != null) equipment.setSerialNo(request.getSerialNo().trim());
        if (request.getStatus() != null) equipment.setStatus(request.getStatus().trim());
        if (request.getCalibrationDueDate() != null) equipment.setCalibrationDueDate(request.getCalibrationDueDate().trim());
        if (request.getModel() != null) equipment.setModel(request.getModel().trim());
        if (request.getSupplier() != null) equipment.setSupplier(request.getSupplier().trim());
        if (request.getPurchaseDate() != null) equipment.setPurchaseDate(request.getPurchaseDate().trim());
        if (request.getWarrantyExpiry() != null) equipment.setWarrantyExpiry(request.getWarrantyExpiry().trim());
        if (request.getDepreciationPercentage() != null) equipment.setDepreciationPercentage(request.getDepreciationPercentage());
        if (request.getInitialValue() != null) equipment.setInitialValue(request.getInitialValue());
        if (request.getCurrentValue() != null) equipment.setCurrentValue(request.getCurrentValue());
        if (request.getAlertMessage() != null) equipment.setAlertMessage(request.getAlertMessage());

        equipment.setUpdatedAt(LocalDateTime.now());
        Equipment updated = equipmentRepository.save(equipment);
        return mapToDto(updated);
    }

    @Override
    public EquipmentResponseDto updateEquipmentStatus(String id, String status) {
        Equipment equipment = findEntity(id);
        equipment.setStatus(status.trim());
        equipment.setUpdatedAt(LocalDateTime.now());
        Equipment updated = equipmentRepository.save(equipment);
        return mapToDto(updated);
    }

    @Override
    public void deleteEquipment(String id) {
        Equipment equipment = findEntity(id);
        equipmentRepository.delete(equipment);
    }

    @Override
    public EquipmentStatsDto getEquipmentStats() {
        List<Equipment> all = equipmentRepository.findAll();
        long total = all.size();
        long inUse = all.stream().filter(e -> "In Use".equalsIgnoreCase(e.getStatus())).count();
        long available = all.stream().filter(e -> "Available".equalsIgnoreCase(e.getStatus())).count();
        long maintenance = all.stream().filter(e -> "Maintenance".equalsIgnoreCase(e.getStatus())).count();
        
        double operationalRate = total > 0 ? ((double) (inUse + available) / total) * 100.0 : 94.2;

        return EquipmentStatsDto.builder()
                .totalInventory(total > 0 ? total : 1240)
                .operationalRate(Math.round(operationalRate * 10.0) / 10.0)
                .underMaintenance(maintenance)
                .calibrationDue(7)
                .build();
    }

    @Override
    public List<String> getLocationsByDepartment(String department) {
        if (department == null || department.trim().isEmpty() || "All".equalsIgnoreCase(department.trim())) {
            List<Bed> allBeds = bedRepository.findAll();
            if (allBeds != null && !allBeds.isEmpty()) {
                return allBeds.stream()
                        .map(b -> b.getWard() + " - Bed " + b.getCode())
                        .distinct()
                        .collect(Collectors.toList());
            }
            return Arrays.asList("ICU - Bed 101", "ICU - Bed 102", "ICU - Bed 103", "ICU - Bed 104");
        }

        String deptStr = department.trim();
        List<String> locations = new ArrayList<>();

        // 1. Fetch matching beds from DB (case-insensitive substring match)
        List<Bed> allBeds = bedRepository.findAll();
        if (allBeds != null) {
            for (Bed b : allBeds) {
                if (b.getWard() != null) {
                    String wardLower = b.getWard().toLowerCase();
                    String deptLower = deptStr.toLowerCase();
                    boolean matches = wardLower.contains(deptLower) || deptLower.contains(wardLower)
                            || ("er".equals(deptLower) && wardLower.contains("emergency"))
                            || ("surgery".equals(deptLower) && wardLower.contains("surgical"));
                    if (matches) {
                        locations.add(b.getWard() + " - Bed " + b.getCode());
                    }
                }
            }
        }

        // 2. Fetch custom department location from DepartmentRepository if set
        departmentRepository.findByNameIgnoreCase(deptStr).ifPresent(d -> {
            if (d.getLocation() != null && !d.getLocation().trim().isEmpty() && !locations.contains(d.getLocation().trim())) {
                locations.add(d.getLocation().trim());
            }
        });

        // 3. Department-specific room / suite fallbacks
        if (deptStr.equalsIgnoreCase("Radiology")) {
            locations.addAll(Arrays.asList("Radiology - Room 1", "Radiology - Room 2", "Radiology - MRI Bay", "Radiology - CT Suite"));
        } else if (deptStr.equalsIgnoreCase("Surgery") || deptStr.equalsIgnoreCase("Surgical")) {
            locations.addAll(Arrays.asList("OR - Suite 1", "OR - Suite 2", "OR - Suite 3", "OR - Recovery Room"));
        } else if (deptStr.equalsIgnoreCase("Biomed Workshop")) {
            locations.addAll(Arrays.asList("Biomed Workshop", "Storage Room A", "Storage Room B", "Central Depot"));
        }

        // 4. Default fallback if empty
        if (locations.isEmpty()) {
            locations.add(deptStr + " - Station 1");
            locations.add(deptStr + " - Storage");
        }

        return locations.stream().distinct().collect(Collectors.toList());
    }

    private Equipment findEntity(String id) {
        return equipmentRepository.findById(id)
                .or(() -> equipmentRepository.findByAssetId(id))
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found with ID: " + id));
    }

    private EquipmentResponseDto mapToDto(Equipment e) {
        return EquipmentResponseDto.builder()
                .id(e.getId())
                .assetId(e.getAssetId())
                .name(e.getName())
                .category(e.getCategory())
                .department(e.getDepartment())
                .location(e.getLocation())
                .serialNo(e.getSerialNo())
                .status(e.getStatus())
                .calibrationDueDate(e.getCalibrationDueDate())
                .model(e.getModel())
                .supplier(e.getSupplier())
                .purchaseDate(e.getPurchaseDate())
                .warrantyExpiry(e.getWarrantyExpiry())
                .depreciationPercentage(e.getDepreciationPercentage())
                .initialValue(e.getInitialValue())
                .currentValue(e.getCurrentValue())
                .alertMessage(e.getAlertMessage())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
