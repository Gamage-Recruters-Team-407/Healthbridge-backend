package com.healthbridge.doctor.service;

import com.healthbridge.doctor.dto.AvailabilityRequestDTO;
import com.healthbridge.doctor.model.DoctorAvailability;
import java.util.List;

public interface DoctorAvailabilityService {
    DoctorAvailability addAvailability(String doctorId, AvailabilityRequestDTO request);
    List<DoctorAvailability> getAvailability(String doctorId);
    DoctorAvailability updateAvailability(String doctorId, String availabilityId, AvailabilityRequestDTO request);
    void deleteAvailability(String doctorId, String availabilityId);
}
