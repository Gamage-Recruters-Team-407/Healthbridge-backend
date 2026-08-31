package com.healthbridge.doctor.controller;

import com.healthbridge.doctor.dto.*;
import com.healthbridge.doctor.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {
    private final DoctorService service;

    @PostMapping
    public ResponseEntity<DoctorResponseDTO> create(@Valid @RequestBody DoctorRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createDoctor(request));
    }
    @GetMapping
    public ResponseEntity<List<DoctorResponseDTO>> getAll() { return ResponseEntity.ok(service.getAllDoctors()); }
    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponseDTO> getById(@PathVariable String id) { return ResponseEntity.ok(service.getDoctorById(id)); }
    @PutMapping("/{id}")
    public ResponseEntity<DoctorResponseDTO> update(@PathVariable String id, @Valid @RequestBody DoctorRequestDTO request) {
        return ResponseEntity.ok(service.updateDoctor(id, request));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.deleteDoctor(id);
        return ResponseEntity.noContent().build();
    }
}
