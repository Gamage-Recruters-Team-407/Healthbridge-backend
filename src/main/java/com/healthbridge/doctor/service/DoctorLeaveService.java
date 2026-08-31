package com.healthbridge.doctor.service;

import com.healthbridge.doctor.dto.LeaveRequestDTO;
import com.healthbridge.doctor.model.DoctorLeave;
import java.util.List;

public interface DoctorLeaveService {
    DoctorLeave applyLeave(String doctorId, LeaveRequestDTO request);
    List<DoctorLeave> getLeaves(String doctorId);
    DoctorLeave updateLeaveStatus(String doctorId, String leaveId, String status);
}
