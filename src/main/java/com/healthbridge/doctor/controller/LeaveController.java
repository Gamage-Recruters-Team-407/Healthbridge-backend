package com.healthbridge.doctor.controller;

import com.healthbridge.doctor.dto.LeaveRequestDTO;
import com.healthbridge.doctor.model.DoctorLeave;
import com.healthbridge.doctor.service.DoctorLeaveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/doctors/{doctorId}/leaves")
@RequiredArgsConstructor
public class LeaveController {
    private final DoctorLeaveService service;

    @PostMapping
    public ResponseEntity<DoctorLeave> apply(@PathVariable String doctorId, @Valid @RequestBody LeaveRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.applyLeave(doctorId, request));
    }
    @GetMapping
    public ResponseEntity<List<DoctorLeave>> get(@PathVariable String doctorId) {
        return ResponseEntity.ok(service.getLeaves(doctorId));
    }
    @PutMapping("/{leaveId}")
    public ResponseEntity<DoctorLeave> updateStatus(@PathVariable String doctorId, @PathVariable String leaveId,
                                                    @RequestParam String status) {
        return ResponseEntity.ok(service.updateLeaveStatus(doctorId, leaveId, status));
    }
}
