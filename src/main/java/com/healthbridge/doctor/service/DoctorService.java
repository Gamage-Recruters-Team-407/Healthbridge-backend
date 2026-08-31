package com.healthbridge.doctor.service;

import com.healthbridge.doctor.dto.*;
import java.util.List;

public interface DoctorService {
    DoctorResponseDTO createDoctor(DoctorRequestDTO request);
    DoctorResponseDTO getDoctorById(String id);
    List<DoctorResponseDTO> getAllDoctors();
    DoctorResponseDTO updateDoctor(String id, DoctorRequestDTO request);
    void deleteDoctor(String id);
}
