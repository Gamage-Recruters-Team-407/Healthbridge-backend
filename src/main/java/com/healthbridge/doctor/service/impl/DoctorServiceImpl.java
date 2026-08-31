package com.healthbridge.doctor.service.impl;

import com.healthbridge.doctor.dto.*;
import com.healthbridge.doctor.exception.*;
import com.healthbridge.doctor.model.Doctor;
import com.healthbridge.doctor.repository.DoctorRepository;
import com.healthbridge.doctor.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {
    private final DoctorRepository repository;

    @Override
    public DoctorResponseDTO createDoctor(DoctorRequestDTO request) {
        repository.findByEmail(request.getEmail().trim().toLowerCase()).ifPresent(d -> {
            throw new DoctorConflictException("A doctor with this email already exists");
        });
        LocalDateTime now = LocalDateTime.now();
        Doctor doctor = Doctor.builder()
                .fullName(request.getFullName().trim())
                .email(request.getEmail().trim().toLowerCase())
                .phone(request.getPhone().trim())
                .specialization(request.getSpecialization().trim())
                .qualifications(request.getQualifications().trim())
                .experience(request.getExperience())
                .consultationFee(request.getConsultationFee())
                .availableStatus(true).createdAt(now).updatedAt(now).build();
        return toResponse(repository.save(doctor));
    }

    @Override
    public DoctorResponseDTO getDoctorById(String id) { return toResponse(find(id)); }

    @Override
    public List<DoctorResponseDTO> getAllDoctors() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public DoctorResponseDTO updateDoctor(String id, DoctorRequestDTO request) {
        Doctor doctor = find(id);
        repository.findByEmail(request.getEmail().trim().toLowerCase())
                .filter(found -> !found.getId().equals(id))
                .ifPresent(found -> { throw new DoctorConflictException("A doctor with this email already exists"); });
        doctor.setFullName(request.getFullName().trim());
        doctor.setEmail(request.getEmail().trim().toLowerCase());
        doctor.setPhone(request.getPhone().trim());
        doctor.setSpecialization(request.getSpecialization().trim());
        doctor.setQualifications(request.getQualifications().trim());
        doctor.setExperience(request.getExperience());
        doctor.setConsultationFee(request.getConsultationFee());
        doctor.setUpdatedAt(LocalDateTime.now());
        return toResponse(repository.save(doctor));
    }

    @Override
    public void deleteDoctor(String id) { repository.delete(find(id)); }

    private Doctor find(String id) {
        return repository.findById(id).orElseThrow(() -> new DoctorResourceNotFoundException("Doctor not found: " + id));
    }

    private DoctorResponseDTO toResponse(Doctor doctor) {
        return DoctorResponseDTO.builder().id(doctor.getId()).fullName(doctor.getFullName())
                .email(doctor.getEmail()).specialization(doctor.getSpecialization())
                .qualifications(doctor.getQualifications()).experience(doctor.getExperience())
                .consultationFee(doctor.getConsultationFee()).build();
    }
}
