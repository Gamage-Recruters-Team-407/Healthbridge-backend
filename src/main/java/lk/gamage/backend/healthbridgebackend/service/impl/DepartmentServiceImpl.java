package lk.gamage.backend.healthbridgebackend.service.impl;

import jakarta.annotation.PostConstruct;
import lk.gamage.backend.healthbridgebackend.dto.DepartmentRequestDto;
import lk.gamage.backend.healthbridgebackend.dto.DepartmentResponseDto;
import lk.gamage.backend.healthbridgebackend.dto.DepartmentStatsDto;
import lk.gamage.backend.healthbridgebackend.exception.AlreadyExistsException;
import lk.gamage.backend.healthbridgebackend.exception.BadRequestException;
import lk.gamage.backend.healthbridgebackend.exception.ResourceNotFoundException;
import lk.gamage.backend.healthbridgebackend.model.Department;
import lk.gamage.backend.healthbridgebackend.repository.DepartmentRepository;
import lk.gamage.backend.healthbridgebackend.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    @PostConstruct
    public void seedInitialData() {
        if (departmentRepository.count() == 0) {
            List<Department> initialDepartments = Arrays.asList(
                    Department.builder().departmentId("DEP-001").name("Cardiology").head("Dr. Nimal Perera").doctorsCount(12).staffCount(24).location("Floor 02").status("Active").contactEmail("cardiology@healthbridge.lk").contactPhone("+94 11 234 5671").description("Specialized unit for cardiovascular disease diagnosis, treatment, and cardiac care.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                    Department.builder().departmentId("DEP-002").name("Neurology").head("Dr. Sarah Fernando").doctorsCount(8).staffCount(18).location("Floor 03").status("Active").contactEmail("neurology@healthbridge.lk").contactPhone("+94 11 234 5672").description("Comprehensive neurological disorders, brain injury, and spinal treatment center.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                    Department.builder().departmentId("DEP-003").name("Pediatrics").head("Dr. Ayesha Silva").doctorsCount(10).staffCount(22).location("Floor 01").status("Active").contactEmail("pediatrics@healthbridge.lk").contactPhone("+94 11 234 5673").description("Dedicated medical care for infants, children, and adolescents.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                    Department.builder().departmentId("DEP-004").name("Dermatology").head("Dr. Kevin Perera").doctorsCount(5).staffCount(10).location("Floor 04").status("Inactive").contactEmail("dermatology@healthbridge.lk").contactPhone("+94 11 234 5674").description("Skin care, cosmetic dermatology, and dermatological surgical treatments.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                    Department.builder().departmentId("DEP-005").name("Orthopedics").head("Dr. Ruwan Bandara").doctorsCount(14).staffCount(30).location("Floor 02").status("Active").contactEmail("orthopedics@healthbridge.lk").contactPhone("+94 11 234 5675").description("Bone, joint, and musculoskeletal system trauma and surgical specialists.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                    Department.builder().departmentId("DEP-006").name("Oncology").head("Dr. Priyantha Jayasinghe").doctorsCount(9).staffCount(20).location("Floor 05").status("Active").contactEmail("oncology@healthbridge.lk").contactPhone("+94 11 234 5676").description("Cancer diagnosis, chemotherapy, radiation therapy, and patient support.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                    Department.builder().departmentId("DEP-007").name("Emergency").head("Dr. Champa Wickramasinghe").doctorsCount(15).staffCount(45).location("Ground Floor").status("Active").contactEmail("emergency@healthbridge.lk").contactPhone("+94 11 234 5677").description("24/7 acute emergency care and intensive trauma response unit.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                    Department.builder().departmentId("DEP-008").name("Radiology").head("Dr. Dilshan Senanayake").doctorsCount(7).staffCount(16).location("Basement 01").status("Active").contactEmail("radiology@healthbridge.lk").contactPhone("+94 11 234 5678").description("Advanced medical imaging including MRI, CT scan, Ultrasound, and X-Ray.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                    Department.builder().departmentId("DEP-009").name("Gastroenterology").head("Dr. Mahesh Cooray").doctorsCount(6).staffCount(12).location("Floor 03").status("Inactive").contactEmail("gastro@healthbridge.lk").contactPhone("+94 11 234 5679").description("Digestive system, liver, and gastrointestinal clinical services.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                    Department.builder().departmentId("DEP-010").name("Psychiatry").head("Dr. Sanduni Rajapaksha").doctorsCount(4).staffCount(8).location("Floor 04").status("Active").contactEmail("psychiatry@healthbridge.lk").contactPhone("+94 11 234 5680").description("Mental health assessment, counseling, and psychiatric therapy.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                    Department.builder().departmentId("DEP-011").name("Nephrology").head("Dr. Thilina De Silva").doctorsCount(5).staffCount(11).location("Floor 02").status("Active").contactEmail("nephrology@healthbridge.lk").contactPhone("+94 11 234 5681").description("Kidney care, renal disease management, and hemodialysis center.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                    Department.builder().departmentId("DEP-012").name("Ophthalmology").head("Dr. Kamal Gunaratne").doctorsCount(3).staffCount(7).location("Floor 01").status("Active").contactEmail("eye@healthbridge.lk").contactPhone("+94 11 234 5682").description("Eye diseases, vision testing, and ophthalmic laser surgery.").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build()
            );
            departmentRepository.saveAll(initialDepartments);
        }
    }

    @Override
    public DepartmentResponseDto createDepartment(DepartmentRequestDto request) {
        validateRequest(request, null);

        String deptId = request.getDepartmentId();
        if (deptId == null || deptId.trim().isEmpty()) {
            long nextNum = departmentRepository.count() + 1;
            deptId = String.format("DEP-%03d", nextNum);
        } else {
            if (departmentRepository.existsByDepartmentId(deptId.trim())) {
                throw new AlreadyExistsException("Department ID '" + deptId.trim() + "' is already in use.");
            }
        }

        String formattedStatus = normalizeStatus(request.getStatus());

        Department department = Department.builder()
                .departmentId(deptId.trim())
                .name(request.getName().trim())
                .head(request.getHead().trim())
                .doctorsCount(request.getDoctorsCount() != null ? request.getDoctorsCount() : 0)
                .staffCount(request.getStaffCount() != null ? request.getStaffCount() : 0)
                .location(request.getLocation().trim())
                .status(formattedStatus)
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .contactEmail(request.getContactEmail() != null ? request.getContactEmail().trim() : null)
                .contactPhone(request.getContactPhone() != null ? request.getContactPhone().trim() : null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Department saved = departmentRepository.save(department);
        return mapToDto(saved);
    }

    @Override
    public List<DepartmentResponseDto> getAllDepartments(String status, String search) {
        List<Department> departments;

        if (status != null && !status.trim().isEmpty() && !"All".equalsIgnoreCase(status)) {
            departments = departmentRepository.findByStatusIgnoreCase(status.trim());
        } else {
            departments = departmentRepository.findAll();
        }

        if (search != null && !search.trim().isEmpty()) {
            String lowerSearch = search.trim().toLowerCase();
            departments = departments.stream()
                    .filter(d -> (d.getName() != null && d.getName().toLowerCase().contains(lowerSearch)) ||
                            (d.getDepartmentId() != null && d.getDepartmentId().toLowerCase().contains(lowerSearch)) ||
                            (d.getHead() != null && d.getHead().toLowerCase().contains(lowerSearch)) ||
                            (d.getLocation() != null && d.getLocation().toLowerCase().contains(lowerSearch)))
                    .collect(Collectors.toList());
        }

        return departments.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public DepartmentResponseDto getDepartmentById(String id) {
        Department department = departmentRepository.findById(id)
                .orElseGet(() -> departmentRepository.findByDepartmentId(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + id)));
        return mapToDto(department);
    }

    @Override
    public DepartmentResponseDto updateDepartment(String id, DepartmentRequestDto request) {
        Department department = departmentRepository.findById(id)
                .orElseGet(() -> departmentRepository.findByDepartmentId(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + id)));

        validateRequestForUpdate(request, department.getId());

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            department.setName(request.getName().trim());
        }
        if (request.getHead() != null && !request.getHead().trim().isEmpty()) {
            department.setHead(request.getHead().trim());
        }
        if (request.getDoctorsCount() != null) {
            if (request.getDoctorsCount() < 0) {
                throw new BadRequestException("Doctors count cannot be negative.");
            }
            department.setDoctorsCount(request.getDoctorsCount());
        }
        if (request.getStaffCount() != null) {
            if (request.getStaffCount() < 0) {
                throw new BadRequestException("Staff count cannot be negative.");
            }
            department.setStaffCount(request.getStaffCount());
        }
        if (request.getLocation() != null && !request.getLocation().trim().isEmpty()) {
            department.setLocation(request.getLocation().trim());
        }
        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            department.setStatus(normalizeStatus(request.getStatus()));
        }
        if (request.getDescription() != null) {
            department.setDescription(request.getDescription().trim());
        }
        if (request.getContactEmail() != null) {
            validateEmail(request.getContactEmail());
            department.setContactEmail(request.getContactEmail().trim());
        }
        if (request.getContactPhone() != null) {
            department.setContactPhone(request.getContactPhone().trim());
        }

        department.setUpdatedAt(LocalDateTime.now());

        Department updated = departmentRepository.save(department);
        return mapToDto(updated);
    }

    @Override
    public DepartmentResponseDto updateDepartmentStatus(String id, String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new BadRequestException("Status parameter cannot be null or empty.");
        }
        String normalized = normalizeStatus(status);

        Department department = departmentRepository.findById(id)
                .orElseGet(() -> departmentRepository.findByDepartmentId(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + id)));

        department.setStatus(normalized);
        department.setUpdatedAt(LocalDateTime.now());

        Department updated = departmentRepository.save(department);
        return mapToDto(updated);
    }

    @Override
    public void deleteDepartment(String id) {
        Department department = departmentRepository.findById(id)
                .orElseGet(() -> departmentRepository.findByDepartmentId(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + id)));

        departmentRepository.delete(department);
    }

    @Override
    public DepartmentStatsDto getDepartmentStats() {
        List<Department> departments = departmentRepository.findAll();

        long totalDepartments = departments.size();
        long activeDepartments = departments.stream()
                .filter(d -> "Active".equalsIgnoreCase(d.getStatus()))
                .count();
        long totalDoctors = departments.stream()
                .mapToLong(d -> d.getDoctorsCount() != null ? d.getDoctorsCount() : 0)
                .sum();
        long totalStaff = departments.stream()
                .mapToLong(d -> d.getStaffCount() != null ? d.getStaffCount() : 0)
                .sum();

        return DepartmentStatsDto.builder()
                .totalDepartments(totalDepartments)
                .activeDepartments(activeDepartments)
                .totalDoctors(totalDoctors)
                .totalDepartmentStaff(totalStaff)
                .build();
    }

    private void validateRequest(DepartmentRequestDto request, String existingId) {
        if (request == null) {
            throw new BadRequestException("Request body cannot be null.");
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BadRequestException("Department name is required.");
        }
        if (request.getHead() == null || request.getHead().trim().isEmpty()) {
            throw new BadRequestException("Department head is required.");
        }
        if (request.getLocation() == null || request.getLocation().trim().isEmpty()) {
            throw new BadRequestException("Department location is required.");
        }
        if (request.getDoctorsCount() != null && request.getDoctorsCount() < 0) {
            throw new BadRequestException("Doctors count cannot be negative.");
        }
        if (request.getStaffCount() != null && request.getStaffCount() < 0) {
            throw new BadRequestException("Staff count cannot be negative.");
        }
        validateEmail(request.getContactEmail());

        departmentRepository.findByNameIgnoreCase(request.getName().trim()).ifPresent(existing -> {
            if (existingId == null || !existing.getId().equals(existingId)) {
                throw new AlreadyExistsException("A department with the name '" + request.getName().trim() + "' already exists.");
            }
        });
    }

    private void validateRequestForUpdate(DepartmentRequestDto request, String existingId) {
        if (request == null) {
            throw new BadRequestException("Request body cannot be null.");
        }
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            departmentRepository.findByNameIgnoreCase(request.getName().trim()).ifPresent(existing -> {
                if (!existing.getId().equals(existingId)) {
                    throw new AlreadyExistsException("A department with the name '" + request.getName().trim() + "' already exists.");
                }
            });
        }
        if (request.getContactEmail() != null) {
            validateEmail(request.getContactEmail());
        }
    }

    private void validateEmail(String email) {
        if (email != null && !email.trim().isEmpty()) {
            if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
                throw new BadRequestException("Invalid email format for department contact email.");
            }
        }
    }

    private String normalizeStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return "Active";
        }
        if ("Inactive".equalsIgnoreCase(status.trim())) {
            return "Inactive";
        }
        return "Active";
    }

    private DepartmentResponseDto mapToDto(Department department) {
        return DepartmentResponseDto.builder()
                .id(department.getId())
                .departmentId(department.getDepartmentId() != null ? department.getDepartmentId() : department.getId())
                .name(department.getName())
                .head(department.getHead())
                .doctorsCount(department.getDoctorsCount() != null ? department.getDoctorsCount() : 0)
                .staffCount(department.getStaffCount() != null ? department.getStaffCount() : 0)
                .location(department.getLocation())
                .status(department.getStatus() != null ? department.getStatus() : "Active")
                .description(department.getDescription())
                .contactEmail(department.getContactEmail())
                .contactPhone(department.getContactPhone())
                .createdAt(department.getCreatedAt())
                .updatedAt(department.getUpdatedAt())
                .build();
    }
}
