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



    private final TreatmentRecordService treatmentRecordService;



    public TreatmentRecordController(
            TreatmentRecordService treatmentRecordService
    ) {
        this.treatmentRecordService = treatmentRecordService;
    }






    @PostMapping
    public ResponseEntity<TreatmentRecordResponse> createTreatment(
            @Valid
            @RequestBody
            TreatmentRecordRequest request
    ) {


        TreatmentRecordResponse response =
                treatmentRecordService.createTreatment(request);


        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);

    }







    @GetMapping
    public ResponseEntity<List<TreatmentRecordResponse>>
    getAllTreatments() {


        return ResponseEntity.ok(
                treatmentRecordService.getAllTreatments()
        );

    }







    @GetMapping("/{id}")
    public ResponseEntity<TreatmentRecordResponse>
    getTreatmentById(
            @PathVariable String id
    ) {


        return ResponseEntity.ok(
                treatmentRecordService.getTreatmentById(id)
        );

    }







    @GetMapping("/record/{medicalRecordId}")
    public ResponseEntity<List<TreatmentRecordResponse>>
    getTreatmentsByMedicalRecord(
            @PathVariable String medicalRecordId
    ) {


        return ResponseEntity.ok(
                treatmentRecordService
                        .getTreatmentsByMedicalRecord(
                                medicalRecordId
                        )
        );

    }







    @PutMapping("/{id}")
    public ResponseEntity<TreatmentRecordResponse>
    updateTreatment(
            @PathVariable String id,

            @Valid
            @RequestBody
            TreatmentRecordRequest request
    ) {


        return ResponseEntity.ok(
                treatmentRecordService.updateTreatment(
                        id,
                        request
                )
        );

    }







    @DeleteMapping("/{id}")
    public ResponseEntity<Void>
    deleteTreatment(
            @PathVariable String id
    ) {


        treatmentRecordService.deleteTreatment(id);


        return ResponseEntity
                .noContent()
                .build();

    }

}