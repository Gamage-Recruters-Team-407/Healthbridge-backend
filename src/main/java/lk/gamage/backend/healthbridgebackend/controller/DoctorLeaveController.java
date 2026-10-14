package lk.gamage.backend.healthbridgebackend.controller;

import lk.gamage.backend.healthbridgebackend.model.DoctorLeave;
import lk.gamage.backend.healthbridgebackend.repository.DoctorLeaveRepository;
import lk.gamage.backend.healthbridgebackend.security.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/doctors/me/leaves")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000", "http://localhost:3001", "http://localhost:3002", "http://localhost:3003"})
public class DoctorLeaveController {

    private final DoctorLeaveRepository leaveRepository;

    public DoctorLeaveController(DoctorLeaveRepository leaveRepository) {
        this.leaveRepository = leaveRepository;
    }

    public record LeaveRequest(String leaveType, String startDate, String endDate, String reason) {}

    @GetMapping
    public ResponseEntity<List<DoctorLeave>> getLeaves(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(required = false) String doctorId,
            @RequestParam(required = false) String email) {

        String targetId = principal != null ? principal.getId() : (doctorId != null ? doctorId : "");
        String targetEmail = principal != null ? principal.getUsername() : (email != null ? email : "");

        List<DoctorLeave> leaves = new ArrayList<>();
        if (!targetId.isBlank()) {
            leaves = leaveRepository.findByDoctorIdOrderByCreatedAtDesc(targetId);
        }
        if (leaves.isEmpty() && !targetEmail.isBlank()) {
            leaves = leaveRepository.findByDoctorEmailOrderByCreatedAtDesc(targetEmail);
        }

        return ResponseEntity.ok(leaves);
    }

    @PostMapping
    public ResponseEntity<DoctorLeave> createLeave(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(required = false) String doctorId,
            @RequestParam(required = false) String email,
            @RequestBody LeaveRequest request) {

        String targetId = principal != null ? principal.getId() : (doctorId != null ? doctorId : "doc-user");
        String targetEmail = principal != null ? principal.getUsername() : (email != null ? email : "");

        DoctorLeave leave = new DoctorLeave();
        leave.setDoctorId(targetId);
        leave.setDoctorEmail(targetEmail);
        leave.setLeaveType(request.leaveType() != null ? request.leaveType() : "Annual");
        leave.setStartDate(request.startDate());
        leave.setEndDate(request.endDate());
        leave.setReason(request.reason());
        leave.setStatus("Pending");
        leave.setAppliedAt(LocalDate.now().toString());
        leave.setCreatedAt(LocalDateTime.now());
        leave.setUpdatedAt(LocalDateTime.now());

        DoctorLeave saved = leaveRepository.save(leave);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLeave(@PathVariable String id) {
        leaveRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
