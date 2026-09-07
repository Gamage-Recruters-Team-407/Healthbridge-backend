package lk.gamage.backend.healthbridgebackend.controller;

import lk.gamage.backend.healthbridgebackend.dto.response.PatientEhrHistoryResponse;
import lk.gamage.backend.healthbridgebackend.service.PatientEhrHistoryService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/medical-records/patient")
public class PatientEhrHistoryController {


    private final PatientEhrHistoryService
            patientEhrHistoryService;


    public PatientEhrHistoryController(
            PatientEhrHistoryService patientEhrHistoryService
    ) {

        this.patientEhrHistoryService =
                patientEhrHistoryService;
    }


    @GetMapping("/{patientId}/history")
    public ResponseEntity<PatientEhrHistoryResponse>
    getPatientEhrHistory(

            @PathVariable
            String patientId
    ) {

        return ResponseEntity.ok(
                patientEhrHistoryService
                        .getPatientEhrHistory(
                                patientId
                        )
        );
    }
}