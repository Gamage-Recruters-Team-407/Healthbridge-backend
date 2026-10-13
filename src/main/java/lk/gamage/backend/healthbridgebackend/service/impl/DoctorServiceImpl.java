package lk.gamage.backend.healthbridgebackend.service.impl;

import lk.gamage.backend.healthbridgebackend.dto.DoctorRequestDto;
import lk.gamage.backend.healthbridgebackend.dto.DoctorResponseDto;
import lk.gamage.backend.healthbridgebackend.model.Doctor;
import lk.gamage.backend.healthbridgebackend.repository.DoctorRepository;
import lk.gamage.backend.healthbridgebackend.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;

    @Override
    public List<DoctorResponseDto> getAllDoctors(String department, String accountStatus, String search) {
        List<Doctor> doctors = doctorRepository.findAll();

        if (department != null && !department.trim().isEmpty() && !department.equals("All departments")) {
            doctors = doctors.stream()
                    .filter(d -> department.equalsIgnoreCase(d.getDepartment()))
                    .collect(Collectors.toList());
        }

        if (accountStatus != null && !accountStatus.trim().isEmpty() && !accountStatus.equals("All statuses")) {
            doctors = doctors.stream()
                    .filter(d -> accountStatus.equalsIgnoreCase(d.getAccountStatus()))
                    .collect(Collectors.toList());
        }

        if (search != null && !search.trim().isEmpty()) {
            String lowercaseSearch = search.toLowerCase();
            doctors = doctors.stream()
                    .filter(d -> 
                        (d.getName() != null && d.getName().toLowerCase().contains(lowercaseSearch)) ||
                        (d.getDoctorId() != null && d.getDoctorId().toLowerCase().contains(lowercaseSearch))
                    )
                    .collect(Collectors.toList());
        }

        return doctors.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public DoctorResponseDto getDoctorById(String id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        return mapToResponse(doctor);
    }

    @Override
    public DoctorResponseDto createDoctor(DoctorRequestDto request) {
        if (request.getEmail() != null && doctorRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        Doctor doctor = new Doctor();
        doctor.setDoctorId(generateDoctorId());
        doctor.setName(request.getName());
        doctor.setDepartment(request.getDepartment());
        doctor.setEmail(request.getEmail());
        doctor.setBranchId(request.getBranchId());
        doctor.setBranchCode(request.getBranchCode());
        doctor.setAccountStatus(request.getAccountStatus() != null ? request.getAccountStatus() : "Active");
        doctor.setCreatedAt(LocalDateTime.now());
        doctor.setUpdatedAt(LocalDateTime.now());

        Doctor savedDoctor = doctorRepository.save(doctor);
        return mapToResponse(savedDoctor);
    }

    @Override
    public DoctorResponseDto updateDoctor(String id, DoctorRequestDto request) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        if (request.getEmail() != null && !request.getEmail().equals(doctor.getEmail()) 
            && doctorRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (request.getName() != null) doctor.setName(request.getName());
        if (request.getDepartment() != null) doctor.setDepartment(request.getDepartment());
        if (request.getEmail() != null) doctor.setEmail(request.getEmail());
        if (request.getBranchId() != null) doctor.setBranchId(request.getBranchId());
        if (request.getBranchCode() != null) doctor.setBranchCode(request.getBranchCode());
        if (request.getAccountStatus() != null) doctor.setAccountStatus(request.getAccountStatus());
        
        doctor.setUpdatedAt(LocalDateTime.now());

        Doctor updatedDoctor = doctorRepository.save(doctor);
        return mapToResponse(updatedDoctor);
    }

    @Override
    public DoctorResponseDto updateAccountStatus(String id, String status) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        
        doctor.setAccountStatus(status);
        doctor.setUpdatedAt(LocalDateTime.now());
        
        return mapToResponse(doctorRepository.save(doctor));
    }

    @Override
    public void deleteDoctor(String id) {
        doctorRepository.deleteById(id);
    }

    private String generateDoctorId() {
        Optional<Doctor> lastDoctor = doctorRepository.findTopByOrderByDoctorIdDesc();
        if (lastDoctor.isEmpty() || lastDoctor.get().getDoctorId() == null) {
            return "DOC-2026-00001";
        }

        String lastId = lastDoctor.get().getDoctorId();
        try {
            String numericPart = lastId.substring(lastId.lastIndexOf("-") + 1);
            int nextNumber = Integer.parseInt(numericPart) + 1;
            return String.format("DOC-2026-%05d", nextNumber);
        } catch (Exception e) {
            return "DOC-2026-00001";
        }
    }

    private DoctorResponseDto mapToResponse(Doctor doctor) {
        return DoctorResponseDto.builder()
                .id(doctor.getId())
                .doctorId(doctor.getDoctorId())
                .name(doctor.getName())
                .department(doctor.getDepartment())
                .email(doctor.getEmail())
                .accountStatus(doctor.getAccountStatus())
                .branchId(doctor.getBranchId())
                .branchCode(doctor.getBranchCode())
                .createdAt(doctor.getCreatedAt())
                .updatedAt(doctor.getUpdatedAt())
                .build();
    }
}
