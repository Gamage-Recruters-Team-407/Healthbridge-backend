package lk.gamage.backend.healthbridgebackend.controller;

import jakarta.validation.Valid;
import lk.gamage.backend.healthbridgebackend.dto.request.MedicalRecordRequest;
import lk.gamage.backend.healthbridgebackend.dto.response.MedicalRecordResponse;
import lk.gamage.backend.healthbridgebackend.service.MedicalRecordService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medical-records")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    public MedicalRecordController(
            MedicalRecordService medicalRecordService
    ) {
        this.medicalRecordService = medicalRecordService;
    }

    @PostMapping
    public ResponseEntity<MedicalRecordResponse>
    createMedicalRecord(
            @Valid
            @RequestBody
            MedicalRecordRequest request
    ) {

        MedicalRecordResponse response =
                medicalRecordService
                        .createMedicalRecord(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<MedicalRecordResponse>>
    getAllMedicalRecords() {

        return ResponseEntity.ok(
                medicalRecordService
                        .getAllMedicalRecords()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicalRecordResponse>
    getMedicalRecordById(
            @PathVariable
            String id
    ) {

        return ResponseEntity.ok(
                medicalRecordService
                        .getMedicalRecordById(id)
        );
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<MedicalRecordResponse>>
    getMedicalRecordsByPatientId(
            @PathVariable
            String patientId
    ) {

        return ResponseEntity.ok(
                medicalRecordService
                        .getMedicalRecordsByPatientId(
                                patientId
                        )
        );
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<MedicalRecordResponse>>
    getMedicalRecordsByDoctorId(
            @PathVariable
            String doctorId
    ) {

        return ResponseEntity.ok(
                medicalRecordService
                        .getMedicalRecordsByDoctorId(
                                doctorId
                        )
        );
    }

    @GetMapping("/archived")
    public ResponseEntity<List<MedicalRecordResponse>>
    getArchivedMedicalRecords() {

        return ResponseEntity.ok(
                medicalRecordService
                        .getArchivedMedicalRecords()
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicalRecordResponse>
    updateMedicalRecord(
            @PathVariable
            String id,

            @Valid
            @RequestBody
            MedicalRecordRequest request
    ) {

        return ResponseEntity.ok(
                medicalRecordService
                        .updateMedicalRecord(
                                id,
                                request
                        )
        );
    }

    @PatchMapping("/{id}/archive")
    public ResponseEntity<MedicalRecordResponse>
    archiveMedicalRecord(
            @PathVariable
            String id
    ) {

        return ResponseEntity.ok(
                medicalRecordService
                        .archiveMedicalRecord(id)
        );
    }

    @PatchMapping("/{id}/restore")
    public ResponseEntity<MedicalRecordResponse>
    restoreMedicalRecord(
            @PathVariable
            String id
    ) {

        return ResponseEntity.ok(
                medicalRecordService
                        .restoreMedicalRecord(id)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void>
    deleteMedicalRecordPermanently(
            @PathVariable
            String id
    ) {

        medicalRecordService
                .deleteMedicalRecordPermanently(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}