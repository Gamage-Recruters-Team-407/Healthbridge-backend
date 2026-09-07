package lk.gamage.backend.healthbridgebackend.controller;

import jakarta.validation.Valid;

import lk.gamage.backend.healthbridgebackend.dto.request.TreatmentRecordRequest;
import lk.gamage.backend.healthbridgebackend.dto.response.TreatmentRecordResponse;
import lk.gamage.backend.healthbridgebackend.service.TreatmentRecordService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/treatments")
public class TreatmentRecordController {


    private final TreatmentRecordService
            treatmentRecordService;


    public TreatmentRecordController(
            TreatmentRecordService treatmentRecordService
    ) {

        this.treatmentRecordService =
                treatmentRecordService;
    }


    /*
     * ---------------------------------------------------------
     * CREATE
     * ---------------------------------------------------------
     */
    @PostMapping
    public ResponseEntity<TreatmentRecordResponse>
    createTreatment(

            @Valid
            @RequestBody
            TreatmentRecordRequest request
    ) {

        TreatmentRecordResponse response =
                treatmentRecordService
                        .createTreatment(
                                request
                        );


        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    /*
     * ---------------------------------------------------------
     * GET ALL
     * ---------------------------------------------------------
     */
    @GetMapping
    public ResponseEntity<List<TreatmentRecordResponse>>
    getAllTreatments() {

        return ResponseEntity.ok(
                treatmentRecordService
                        .getAllTreatments()
        );
    }


    /*
     * ---------------------------------------------------------
     * GET BY MEDICAL RECORD
     * ---------------------------------------------------------
     */
    @GetMapping(
            "/record/{medicalRecordId}"
    )
    public ResponseEntity<List<TreatmentRecordResponse>>
    getTreatmentsByMedicalRecord(

            @PathVariable
            String medicalRecordId
    ) {

        return ResponseEntity.ok(
                treatmentRecordService
                        .getTreatmentsByMedicalRecord(
                                medicalRecordId
                        )
        );
    }


    /*
     * ---------------------------------------------------------
     * GET BY PATIENT
     * ---------------------------------------------------------
     */
    @GetMapping(
            "/patient/{patientId}"
    )
    public ResponseEntity<List<TreatmentRecordResponse>>
    getTreatmentsByPatient(

            @PathVariable
            String patientId
    ) {

        return ResponseEntity.ok(
                treatmentRecordService
                        .getTreatmentsByPatient(
                                patientId
                        )
        );
    }


    /*
     * ---------------------------------------------------------
     * GET BY DOCTOR
     * ---------------------------------------------------------
     */
    @GetMapping(
            "/doctor/{doctorId}"
    )
    public ResponseEntity<List<TreatmentRecordResponse>>
    getTreatmentsByDoctor(

            @PathVariable
            String doctorId
    ) {

        return ResponseEntity.ok(
                treatmentRecordService
                        .getTreatmentsByDoctor(
                                doctorId
                        )
        );
    }


    /*
     * ---------------------------------------------------------
     * GET BY ID
     * ---------------------------------------------------------
     */
    @GetMapping("/{id}")
    public ResponseEntity<TreatmentRecordResponse>
    getTreatmentById(

            @PathVariable
            String id
    ) {

        return ResponseEntity.ok(
                treatmentRecordService
                        .getTreatmentById(
                                id
                        )
        );
    }


    /*
     * ---------------------------------------------------------
     * UPDATE
     * ---------------------------------------------------------
     */
    @PutMapping("/{id}")
    public ResponseEntity<TreatmentRecordResponse>
    updateTreatment(

            @PathVariable
            String id,

            @Valid
            @RequestBody
            TreatmentRecordRequest request
    ) {

        return ResponseEntity.ok(
                treatmentRecordService
                        .updateTreatment(
                                id,
                                request
                        )
        );
    }


    /*
     * ---------------------------------------------------------
     * DELETE
     * ---------------------------------------------------------
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void>
    deleteTreatment(

            @PathVariable
            String id
    ) {

        treatmentRecordService
                .deleteTreatment(
                        id
                );


        return ResponseEntity
                .noContent()
                .build();
    }
}