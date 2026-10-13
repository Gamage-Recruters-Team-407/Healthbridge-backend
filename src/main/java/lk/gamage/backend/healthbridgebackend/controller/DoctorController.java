package lk.gamage.backend.healthbridgebackend.controller;

import lk.gamage.backend.healthbridgebackend.dto.DoctorRequestDto;
import lk.gamage.backend.healthbridgebackend.dto.DoctorResponseDto;
import lk.gamage.backend.healthbridgebackend.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping
    public ResponseEntity<List<DoctorResponseDto>> getAllDoctors(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String accountStatus,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(doctorService.getAllDoctors(department, accountStatus, search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponseDto> getDoctorById(@PathVariable String id) {
        return ResponseEntity.ok(doctorService.getDoctorById(id));
    }

    @PostMapping
    public ResponseEntity<DoctorResponseDto> createDoctor(@RequestBody DoctorRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(doctorService.createDoctor(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DoctorResponseDto> updateDoctor(
            @PathVariable String id,
            @RequestBody DoctorRequestDto request) {
        return ResponseEntity.ok(doctorService.updateDoctor(id, request));
    }

    @PatchMapping("/{id}/account-status")
    public ResponseEntity<DoctorResponseDto> updateAccountStatus(
            @PathVariable String id,
            @RequestParam String status) {
        return ResponseEntity.ok(doctorService.updateAccountStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDoctor(@PathVariable String id) {
        doctorService.deleteDoctor(id);
        return ResponseEntity.noContent().build();
    }
}
