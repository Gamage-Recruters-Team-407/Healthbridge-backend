package lk.gamage.backend.healthbridgebackend.service;

import lk.gamage.backend.healthbridgebackend.dto.DoctorRequestDto;
import lk.gamage.backend.healthbridgebackend.dto.DoctorResponseDto;

import java.util.List;

public interface DoctorService {
    List<DoctorResponseDto> getAllDoctors(String department, String accountStatus, String search);
    DoctorResponseDto getDoctorById(String id);
    DoctorResponseDto createDoctor(DoctorRequestDto request);
    DoctorResponseDto updateDoctor(String id, DoctorRequestDto request);
    DoctorResponseDto updateAccountStatus(String id, String status);
    void deleteDoctor(String id);
}
