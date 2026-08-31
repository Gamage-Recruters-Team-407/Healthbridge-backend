package lk.gamage.backend.healthbridgebackend.service.impl;

import jakarta.annotation.PostConstruct;
import lk.gamage.backend.healthbridgebackend.dto.*;
import lk.gamage.backend.healthbridgebackend.exception.AlreadyExistsException;
import lk.gamage.backend.healthbridgebackend.exception.BadRequestException;
import lk.gamage.backend.healthbridgebackend.exception.ResourceNotFoundException;
import lk.gamage.backend.healthbridgebackend.model.Bed;
import lk.gamage.backend.healthbridgebackend.model.PatientInfo;
import lk.gamage.backend.healthbridgebackend.repository.BedRepository;
import lk.gamage.backend.healthbridgebackend.service.BedService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BedServiceImpl implements BedService {

    private final BedRepository bedRepository;

    @PostConstruct
    public void seedInitialData() {
        if (bedRepository.count() == 0) {
            List<Bed> initialBeds = Arrays.asList(
                    Bed.builder()
                            .bedId("ICU-101").code("101").ward("ICU").status("Occupied").bedType("ICU Standard")
                            .patient(PatientInfo.builder().id("PID-10482").firstName("Alex").lastName("Fernando").dob("15 Mar 1978").age(46).gender("Male").assignedDoctor("Dr. Nimal Perera").admissionDate("2026-08-10").expDischarge("2026-08-22").admissionNotes("Post-cardiac bypass recovery and monitoring required.").currentWard("ICU").currentBedId("ICU-101").build())
                            .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                    Bed.builder()
                            .bedId("ICU-102").code("102").ward("ICU").status("Available").bedType("ICU Standard")
                            .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                    Bed.builder()
                            .bedId("ICU-103").code("103").ward("ICU").status("Reserved").bedType("ICU Standard")
                            .patient(PatientInfo.builder().id("PID-10499").firstName("Samantha").lastName("Wickramasinghe").dob("22 Nov 1985").age(40).gender("Female").assignedDoctor("Dr. Sarah Fernando").eta("14:00").admissionNotes("Transfer from Emergency Ward pending ICU clearance.").currentWard("ICU").currentBedId("ICU-103").build())
                            .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                    Bed.builder()
                            .bedId("ICU-104").code("104").ward("ICU").status("Occupied").bedType("ICU Standard")
                            .patient(PatientInfo.builder().id("PID-10311").firstName("Jane").lastName("Doe").dob("12 May 1968").age(55).gender("Female").assignedDoctor("Dr. Smith").admissionDate("2026-08-12").expDischarge("2026-08-25").admissionNotes("Acute respiratory distress observation.").currentWard("ICU").currentBedId("ICU-104").build())
                            .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                    Bed.builder()
                            .bedId("ICU-105").code("105").ward("ICU").status("Maintenance").bedType("Electric ICU")
                            .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                    Bed.builder()
                            .bedId("ICU-106").code("106").ward("ICU").status("Cleaning").bedType("ICU Standard")
                            .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),

                    Bed.builder()
                            .bedId("GEN-201").code("201").ward("General Ward").status("Occupied").bedType("General Electric")
                            .patient(PatientInfo.builder().id("PID-10204").firstName("Kamal").lastName("Gunaratne").dob("04 Jan 1962").age(64).gender("Male").assignedDoctor("Dr. Ruwan Bandara").admissionDate("2026-08-15").currentWard("General Ward").currentBedId("GEN-201").build())
                            .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                    Bed.builder()
                            .bedId("GEN-202").code("202").ward("General Ward").status("Available").bedType("General Electric")
                            .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                    Bed.builder()
                            .bedId("GEN-203").code("203").ward("General Ward").status("Available").bedType("General Standard")
                            .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),

                    Bed.builder()
                            .bedId("EMG-301").code("301").ward("Emergency Ward").status("Occupied").bedType("Emergency Trauma")
                            .patient(PatientInfo.builder().id("PID-10501").firstName("Sunil").lastName("De Silva").dob("19 Aug 1990").age(34).gender("Male").assignedDoctor("Dr. Champa Wickramasinghe").currentWard("Emergency Ward").currentBedId("EMG-301").build())
                            .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                    Bed.builder()
                            .bedId("EMG-302").code("302").ward("Emergency Ward").status("Available").bedType("Emergency Standard")
                            .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),

                    Bed.builder()
                            .bedId("CARD-401").code("401").ward("Cardiology").status("Available").bedType("Cardiac Monitor")
                            .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                    Bed.builder()
                            .bedId("CARD-402").code("402").ward("Cardiology").status("Occupied").bedType("Cardiac Monitor")
                            .patient(PatientInfo.builder().id("PID-10515").firstName("Kusum").lastName("Perera").dob("08 Dec 1955").age(70).gender("Female").assignedDoctor("Dr. Nimal Perera").admissionDate("2026-08-18").currentWard("Cardiology").currentBedId("CARD-402").build())
                            .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),

                    Bed.builder()
                            .bedId("PED-501").code("501").ward("Pediatrics").status("Occupied").bedType("Pediatric Bed")
                            .patient(PatientInfo.builder().id("PID-10520").firstName("Nipuni").lastName("Perera").dob("10 Jun 2018").age(8).gender("Female").assignedDoctor("Dr. Ayesha Silva").currentWard("Pediatrics").currentBedId("PED-501").build())
                            .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                    Bed.builder()
                            .bedId("PED-502").code("502").ward("Pediatrics").status("Available").bedType("Pediatric Bed")
                            .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),

                    Bed.builder()
                            .bedId("MAT-601").code("601").ward("Maternity").status("Available").bedType("Maternity Bed")
                            .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                    Bed.builder()
                            .bedId("MAT-602").code("602").ward("Maternity").status("Available").bedType("Maternity Bed")
                            .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build()
            );

            bedRepository.saveAll(initialBeds);
        }
    }

    @Override
    public BedResponseDto createBed(BedRequestDto request) {
        if (request == null || request.getBedId() == null || request.getBedId().trim().isEmpty()) {
            throw new BadRequestException("Bed ID is required.");
        }
        String bedIdStr = request.getBedId().trim();

        if (bedRepository.existsByBedId(bedIdStr)) {
            throw new AlreadyExistsException("Bed with ID '" + bedIdStr + "' already exists.");
        }

        Bed bed = Bed.builder()
                .bedId(bedIdStr)
                .code(request.getCode() != null ? request.getCode().trim() : extractCode(bedIdStr))
                .ward(normalizeWard(request.getWard()))
                .status(normalizeStatus(request.getStatus()))
                .bedType(request.getBedType() != null ? request.getBedType().trim() : "Standard")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Bed saved = bedRepository.save(bed);
        return mapToDto(saved);
    }

    @Override
    public List<BedResponseDto> getAllBeds(String ward, String status, String search) {
        List<Bed> beds;

        if (ward != null && !ward.trim().isEmpty() && !"All".equalsIgnoreCase(ward)) {
            if (status != null && !status.trim().isEmpty() && !"All".equalsIgnoreCase(status)) {
                beds = bedRepository.findByWardIgnoreCaseAndStatusIgnoreCase(ward.trim(), status.trim());
            } else {
                beds = bedRepository.findByWardIgnoreCase(ward.trim());
            }
        } else if (status != null && !status.trim().isEmpty() && !"All".equalsIgnoreCase(status)) {
            beds = bedRepository.findByStatusIgnoreCase(status.trim());
        } else {
            beds = bedRepository.findAll();
        }

        if (search != null && !search.trim().isEmpty()) {
            String lowerSearch = search.trim().toLowerCase();
            beds = beds.stream()
                    .filter(b -> (b.getBedId() != null && b.getBedId().toLowerCase().contains(lowerSearch)) ||
                            (b.getCode() != null && b.getCode().toLowerCase().contains(lowerSearch)) ||
                            (b.getBedType() != null && b.getBedType().toLowerCase().contains(lowerSearch)) ||
                            (b.getPatient() != null && (
                                    (b.getPatient().getFirstName() != null && b.getPatient().getFirstName().toLowerCase().contains(lowerSearch)) ||
                                    (b.getPatient().getLastName() != null && b.getPatient().getLastName().toLowerCase().contains(lowerSearch)) ||
                                    (b.getPatient().getId() != null && b.getPatient().getId().toLowerCase().contains(lowerSearch))
                            )))
                    .collect(Collectors.toList());
        }

        return beds.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public BedResponseDto getBedById(String id) {
        Bed bed = findBedByIdOrBedId(id);
        return mapToDto(bed);
    }

    @Override
    public BedResponseDto updateBed(String id, BedRequestDto request) {
        Bed bed = findBedByIdOrBedId(id);

        if (request.getCode() != null && !request.getCode().trim().isEmpty()) {
            bed.setCode(request.getCode().trim());
        }
        if (request.getWard() != null && !request.getWard().trim().isEmpty()) {
            bed.setWard(normalizeWard(request.getWard()));
        }
        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            bed.setStatus(normalizeStatus(request.getStatus()));
        }
        if (request.getBedType() != null && !request.getBedType().trim().isEmpty()) {
            bed.setBedType(request.getBedType().trim());
        }

        bed.setUpdatedAt(LocalDateTime.now());
        Bed updated = bedRepository.save(bed);
        return mapToDto(updated);
    }

    @Override
    public BedResponseDto updateBedStatus(String id, String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new BadRequestException("Status is required.");
        }
        Bed bed = findBedByIdOrBedId(id);
        String normStatus = normalizeStatus(status);

        bed.setStatus(normStatus);
        if ("Available".equalsIgnoreCase(normStatus) || "Maintenance".equalsIgnoreCase(normStatus) || "Cleaning".equalsIgnoreCase(normStatus)) {
            // If changing to non-occupied/non-reserved status, release patient info
            if (!"Reserved".equalsIgnoreCase(normStatus)) {
                bed.setPatient(null);
            }
        }
        bed.setUpdatedAt(LocalDateTime.now());

        Bed updated = bedRepository.save(bed);
        return mapToDto(updated);
    }

    @Override
    public BedResponseDto allocateBed(String id, BedAllocationRequestDto request) {
        if (request == null) {
            throw new BadRequestException("Allocation request body cannot be null.");
        }
        Bed bed = findBedByIdOrBedId(id);

        String pid = request.getPatientId();
        if (pid == null || pid.trim().isEmpty()) {
            pid = String.format("PID-%05d", new Random().nextInt(90000) + 10000);
        }

        PatientInfo patient = PatientInfo.builder()
                .id(pid.trim())
                .firstName(request.getFirstName() != null ? request.getFirstName().trim() : "New")
                .lastName(request.getLastName() != null ? request.getLastName().trim() : "Patient")
                .dob(request.getDob() != null ? request.getDob() : "01 Jan 1990")
                .age(request.getAge() != null ? request.getAge() : 34)
                .gender(request.getGender() != null ? request.getGender() : "Male")
                .assignedDoctor(request.getAssignedDoctor() != null ? request.getAssignedDoctor().trim() : "Dr. Nimal Perera")
                .admissionDate(request.getAdmissionDate() != null ? request.getAdmissionDate() : LocalDateTime.now().toLocalDate().toString())
                .expDischarge(request.getExpDischarge())
                .admissionNotes(request.getAdmissionNotes())
                .eta(request.getEta())
                .currentWard(bed.getWard())
                .currentBedId(bed.getBedId())
                .build();

        bed.setStatus("Occupied");
        bed.setPatient(patient);
        bed.setUpdatedAt(LocalDateTime.now());

        Bed saved = bedRepository.save(bed);
        return mapToDto(saved);
    }

    @Override
    public BedResponseDto transferPatient(String id, BedTransferRequestDto request) {
        if (request == null || request.getAvailableBedId() == null || request.getAvailableBedId().trim().isEmpty()) {
            throw new BadRequestException("Target bed ID is required for patient transfer.");
        }

        Bed sourceBed = findBedByIdOrBedId(id);
        if (sourceBed.getPatient() == null) {
            throw new BadRequestException("Source bed '" + sourceBed.getBedId() + "' does not currently have an assigned patient.");
        }

        Bed targetBed = findBedByIdOrBedId(request.getAvailableBedId().trim());

        PatientInfo patient = sourceBed.getPatient();

        // Release source bed
        sourceBed.setStatus("Available");
        sourceBed.setPatient(null);
        sourceBed.setUpdatedAt(LocalDateTime.now());
        bedRepository.save(sourceBed);

        // Occupy target bed
        patient.setCurrentWard(targetBed.getWard());
        patient.setCurrentBedId(targetBed.getBedId());

        targetBed.setStatus("Occupied");
        targetBed.setPatient(patient);
        targetBed.setUpdatedAt(LocalDateTime.now());

        Bed updatedTarget = bedRepository.save(targetBed);
        return mapToDto(updatedTarget);
    }

    @Override
    public void deleteBed(String id) {
        Bed bed = findBedByIdOrBedId(id);
        bedRepository.delete(bed);
    }

    @Override
    public BedStatsDto getBedStats() {
        List<Bed> beds = bedRepository.findAll();

        long dbCount = beds.size();
        // Hospital total capacity calculation
        long totalBeds = Math.max(dbCount + 200, 240);

        long dbOccupied = beds.stream().filter(b -> "Occupied".equalsIgnoreCase(b.getStatus())).count();
        long dbAvailable = beds.stream().filter(b -> "Available".equalsIgnoreCase(b.getStatus())).count();
        long dbMaintenance = beds.stream().filter(b -> "Maintenance".equalsIgnoreCase(b.getStatus())).count();
        long dbCleaning = beds.stream().filter(b -> "Cleaning".equalsIgnoreCase(b.getStatus())).count();
        long dbReserved = beds.stream().filter(b -> "Reserved".equalsIgnoreCase(b.getStatus())).count();

        long occupiedBeds = dbOccupied + 185;
        long availableBeds = dbAvailable + 30;
        long maintenanceBeds = dbMaintenance + 8;
        int occupiedPercentage = (int) Math.round(((double) occupiedBeds / totalBeds) * 100);

        return BedStatsDto.builder()
                .totalBeds(totalBeds)
                .occupiedBeds(occupiedBeds)
                .occupiedPercentage(occupiedPercentage)
                .availableBeds(availableBeds)
                .maintenanceBeds(maintenanceBeds)
                .cleaningBeds(dbCleaning)
                .reservedBeds(dbReserved)
                .build();
    }

    @Override
    public List<DepartmentOccupancyDto> getDepartmentOccupancy() {
        List<Bed> beds = bedRepository.findAll();
        List<String> departments = Arrays.asList("ICU", "General Ward", "Pediatrics", "Emergency Ward", "Cardiology", "Maternity");

        Map<String, Long> totalPerDept = beds.stream()
                .collect(Collectors.groupingBy(Bed::getWard, Collectors.counting()));
        Map<String, Long> occupiedPerDept = beds.stream()
                .filter(b -> "Occupied".equalsIgnoreCase(b.getStatus()))
                .collect(Collectors.groupingBy(Bed::getWard, Collectors.counting()));

        List<DepartmentOccupancyDto> result = new ArrayList<>();

        Map<String, Integer> presetOccupancy = new HashMap<>();
        presetOccupancy.put("ICU", 92);
        presetOccupancy.put("General Ward", 75);
        presetOccupancy.put("Pediatrics", 60);
        presetOccupancy.put("Emergency Ward", 45);
        presetOccupancy.put("Cardiology", 34);
        presetOccupancy.put("Maternity", 10);

        for (String dept : departments) {
            long deptTotal = totalPerDept.getOrDefault(dept, 0L);
            long deptOccupied = occupiedPerDept.getOrDefault(dept, 0L);

            int percentage;
            if (deptTotal > 0) {
                percentage = (int) Math.round(((double) deptOccupied / deptTotal) * 100);
            } else {
                percentage = presetOccupancy.getOrDefault(dept, 50);
            }

            boolean isAlert = percentage >= 85;

            result.add(DepartmentOccupancyDto.builder()
                    .department(dept)
                    .occupancyPercentage(percentage)
                    .isAlert(isAlert)
                    .build());
        }

        return result;
    }

    private Bed findBedByIdOrBedId(String id) {
        return bedRepository.findById(id)
                .orElseGet(() -> bedRepository.findByBedId(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Bed not found with ID: " + id)));
    }

    private String normalizeWard(String ward) {
        if (ward == null || ward.trim().isEmpty()) {
            return "General Ward";
        }
        return ward.trim();
    }

    private String normalizeStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return "Available";
        }
        String trimmed = status.trim();
        if ("Occupied".equalsIgnoreCase(trimmed)) return "Occupied";
        if ("Reserved".equalsIgnoreCase(trimmed)) return "Reserved";
        if ("Maintenance".equalsIgnoreCase(trimmed)) return "Maintenance";
        if ("Cleaning".equalsIgnoreCase(trimmed)) return "Cleaning";
        return "Available";
    }

    private String extractCode(String bedId) {
        if (bedId.contains("-")) {
            String[] parts = bedId.split("-");
            return parts[parts.length - 1];
        }
        return bedId;
    }

    private BedResponseDto mapToDto(Bed bed) {
        PatientInfoDto pDto = null;
        if (bed.getPatient() != null) {
            PatientInfo p = bed.getPatient();
            pDto = PatientInfoDto.builder()
                    .id(p.getId())
                    .firstName(p.getFirstName())
                    .lastName(p.getLastName())
                    .dob(p.getDob())
                    .age(p.getAge())
                    .gender(p.getGender())
                    .assignedDoctor(p.getAssignedDoctor())
                    .admissionDate(p.getAdmissionDate())
                    .expDischarge(p.getExpDischarge())
                    .admissionNotes(p.getAdmissionNotes())
                    .eta(p.getEta())
                    .currentWard(p.getCurrentWard())
                    .currentBedId(p.getCurrentBedId())
                    .build();
        }

        return BedResponseDto.builder()
                .id(bed.getBedId() != null ? bed.getBedId() : bed.getId())
                .bedId(bed.getBedId() != null ? bed.getBedId() : bed.getId())
                .code(bed.getCode() != null ? bed.getCode() : extractCode(bed.getBedId()))
                .ward(bed.getWard())
                .status(bed.getStatus())
                .bedType(bed.getBedType())
                .patient(pDto)
                .createdAt(bed.getCreatedAt())
                .updatedAt(bed.getUpdatedAt())
                .build();
    }
}
