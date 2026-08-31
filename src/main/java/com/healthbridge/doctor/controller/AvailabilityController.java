package com.healthbridge.doctor.controller;

import com.healthbridge.doctor.dto.AvailabilityRequestDTO;
import com.healthbridge.doctor.model.DoctorAvailability;
import com.healthbridge.doctor.service.DoctorAvailabilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/doctors/{doctorId}/availability")
@RequiredArgsConstructor
public class AvailabilityController {
    private final DoctorAvailabilityService service;

    @PostMapping
    public ResponseEntity<DoctorAvailability> add(@PathVariable String doctorId, @Valid @RequestBody AvailabilityRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addAvailability(doctorId, request));
    }
    @GetMapping
    public ResponseEntity<List<DoctorAvailability>> get(@PathVariable String doctorId) {
        return ResponseEntity.ok(service.getAvailability(doctorId));
    }
    @PutMapping("/{availabilityId}")
    public ResponseEntity<DoctorAvailability> update(@PathVariable String doctorId, @PathVariable String availabilityId,
                                                      @Valid @RequestBody AvailabilityRequestDTO request) {
        return ResponseEntity.ok(service.updateAvailability(doctorId, availabilityId, request));
    }
    @DeleteMapping("/{availabilityId}")
    public ResponseEntity<Void> delete(@PathVariable String doctorId, @PathVariable String availabilityId) {
        service.deleteAvailability(doctorId, availabilityId);
        return ResponseEntity.noContent().build();
    }
}
