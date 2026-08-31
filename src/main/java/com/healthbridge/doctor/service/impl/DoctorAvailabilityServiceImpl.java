package com.healthbridge.doctor.service.impl;

import com.healthbridge.doctor.dto.AvailabilityRequestDTO;
import com.healthbridge.doctor.exception.*;
import com.healthbridge.doctor.model.DoctorAvailability;
import com.healthbridge.doctor.repository.*;
import com.healthbridge.doctor.service.DoctorAvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorAvailabilityServiceImpl implements DoctorAvailabilityService {
    private final DoctorAvailabilityRepository repository;
    private final DoctorRepository doctorRepository;

    @Override
    public DoctorAvailability addAvailability(String doctorId, AvailabilityRequestDTO request) {
        validateDoctorAndRequest(doctorId, request);
        validateTimes(request);
        return repository.save(DoctorAvailability.builder().doctorId(doctorId).date(request.getDate())
                .startTime(request.getStartTime()).endTime(request.getEndTime()).status("AVAILABLE").build());
    }

    @Override
    public List<DoctorAvailability> getAvailability(String doctorId) {
        requireDoctor(doctorId);
        return repository.findByDoctorId(doctorId);
    }

    @Override
    public DoctorAvailability updateAvailability(String doctorId, String availabilityId, AvailabilityRequestDTO request) {
        validateDoctorAndRequest(doctorId, request);
        validateTimes(request);
        DoctorAvailability availability = findOwned(doctorId, availabilityId);
        availability.setDate(request.getDate());
        availability.setStartTime(request.getStartTime());
        availability.setEndTime(request.getEndTime());
        return repository.save(availability);
    }

    @Override
    public void deleteAvailability(String doctorId, String availabilityId) {
        requireDoctor(doctorId);
        repository.delete(findOwned(doctorId, availabilityId));
    }

    private void validateDoctorAndRequest(String doctorId, AvailabilityRequestDTO request) {
        requireDoctor(doctorId);
        if (!doctorId.equals(request.getDoctorId())) throw new DoctorBadRequestException("doctorId must match the URL");
    }
    private void validateTimes(AvailabilityRequestDTO request) {
        if (!request.getEndTime().isAfter(request.getStartTime())) throw new DoctorBadRequestException("endTime must be after startTime");
    }
    private void requireDoctor(String id) {
        if (!doctorRepository.existsById(id)) throw new DoctorResourceNotFoundException("Doctor not found: " + id);
    }
    private DoctorAvailability findOwned(String doctorId, String id) {
        DoctorAvailability value = repository.findById(id).orElseThrow(() -> new DoctorResourceNotFoundException("Availability not found: " + id));
        if (!doctorId.equals(value.getDoctorId())) throw new DoctorResourceNotFoundException("Availability not found: " + id);
        return value;
    }
}
