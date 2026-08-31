package com.healthbridge.doctor.service.impl;

import com.healthbridge.doctor.dto.LeaveRequestDTO;
import com.healthbridge.doctor.exception.*;
import com.healthbridge.doctor.model.DoctorLeave;
import com.healthbridge.doctor.repository.*;
import com.healthbridge.doctor.service.DoctorLeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DoctorLeaveServiceImpl implements DoctorLeaveService {
    private static final Set<String> STATUSES = Set.of("PENDING", "APPROVED", "REJECTED", "CANCELLED");
    private final DoctorLeaveRepository repository;
    private final DoctorRepository doctorRepository;

    @Override
    public DoctorLeave applyLeave(String doctorId, LeaveRequestDTO request) {
        requireDoctor(doctorId);
        if (!doctorId.equals(request.getDoctorId())) throw new DoctorBadRequestException("doctorId must match the URL");
        if (request.getEndDate().isBefore(request.getStartDate())) throw new DoctorBadRequestException("endDate cannot be before startDate");
        return repository.save(DoctorLeave.builder().doctorId(doctorId).leaveType(request.getLeaveType().trim())
                .startDate(request.getStartDate()).endDate(request.getEndDate()).reason(request.getReason().trim())
                .status("PENDING").build());
    }

    @Override
    public List<DoctorLeave> getLeaves(String doctorId) {
        requireDoctor(doctorId);
        return repository.findByDoctorId(doctorId);
    }

    @Override
    public DoctorLeave updateLeaveStatus(String doctorId, String leaveId, String status) {
        requireDoctor(doctorId);
        String normalized = status == null ? "" : status.trim().toUpperCase(Locale.ROOT);
        if (!STATUSES.contains(normalized)) throw new DoctorBadRequestException("status must be PENDING, APPROVED, REJECTED, or CANCELLED");
        DoctorLeave leave = repository.findById(leaveId).orElseThrow(() -> new DoctorResourceNotFoundException("Leave not found: " + leaveId));
        if (!doctorId.equals(leave.getDoctorId())) throw new DoctorResourceNotFoundException("Leave not found: " + leaveId);
        leave.setStatus(normalized);
        return repository.save(leave);
    }

    private void requireDoctor(String id) {
        if (!doctorRepository.existsById(id)) throw new DoctorResourceNotFoundException("Doctor not found: " + id);
    }
}
