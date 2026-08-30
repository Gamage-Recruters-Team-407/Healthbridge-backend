package lk.gamage.backend.healthbridgebackend.controller;


import jakarta.validation.Valid;
import lk.gamage.backend.healthbridgebackend.dto.request.DiagnosisRequest;
import lk.gamage.backend.healthbridgebackend.dto.response.DiagnosisResponse;
import lk.gamage.backend.healthbridgebackend.service.DiagnosisService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;



@RestController
@RequestMapping("/api/diagnoses")
public class DiagnosisController {



    private final DiagnosisService diagnosisService;



    public DiagnosisController(
            DiagnosisService diagnosisService
    ) {
        this.diagnosisService = diagnosisService;
    }





    @PostMapping
    public ResponseEntity<DiagnosisResponse> createDiagnosis(
            @Valid
            @RequestBody
            DiagnosisRequest request
    ) {


        DiagnosisResponse response =
                diagnosisService.createDiagnosis(request);


        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);

    }






    @GetMapping
    public ResponseEntity<List<DiagnosisResponse>> getAllDiagnoses() {


        return ResponseEntity.ok(
                diagnosisService.getAllDiagnoses()
        );

    }







    @GetMapping("/{id}")
    public ResponseEntity<DiagnosisResponse> getDiagnosisById(
            @PathVariable String id
    ) {


        return ResponseEntity.ok(
                diagnosisService.getDiagnosisById(id)
        );

    }







    @GetMapping("/record/{medicalRecordId}")
    public ResponseEntity<List<DiagnosisResponse>>
    getDiagnosesByMedicalRecord(
            @PathVariable String medicalRecordId
    ) {


        return ResponseEntity.ok(
                diagnosisService
                        .getDiagnosesByMedicalRecord(
                                medicalRecordId
                        )
        );

    }







    @PutMapping("/{id}")
    public ResponseEntity<DiagnosisResponse> updateDiagnosis(
            @PathVariable String id,

            @Valid
            @RequestBody
            DiagnosisRequest request
    ) {


        return ResponseEntity.ok(
                diagnosisService.updateDiagnosis(
                        id,
                        request
                )
        );

    }







    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDiagnosis(
            @PathVariable String id
    ) {


        diagnosisService.deleteDiagnosis(id);


        return ResponseEntity
                .noContent()
                .build();

    }

}