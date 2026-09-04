package lk.gamage.backend.healthbridgebackend.controller;

import lk.gamage.backend.healthbridgebackend.dto.request.MedicalDocumentUpdateRequest;
import lk.gamage.backend.healthbridgebackend.dto.response.MedicalDocumentResponse;
import lk.gamage.backend.healthbridgebackend.service.MedicalDocumentService;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequestMapping("/api/medical-documents")
public class MedicalDocumentController {

    private final MedicalDocumentService
            medicalDocumentService;


    public MedicalDocumentController(
            MedicalDocumentService medicalDocumentService
    ) {

        this.medicalDocumentService =
                medicalDocumentService;
    }


    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<MedicalDocumentResponse>
    uploadMedicalDocument(

            @RequestParam("file")
            MultipartFile file,

            @RequestParam("medicalRecordId")
            String medicalRecordId,

            @RequestParam("patientId")
            String patientId,

            @RequestParam("doctorId")
            String doctorId,

            @RequestParam("documentType")
            String documentType,

            @RequestParam(
                    value = "description",
                    required = false
            )
            String description
    ) {

        MedicalDocumentResponse response =
                medicalDocumentService
                        .uploadDocument(
                                file,
                                medicalRecordId,
                                patientId,
                                doctorId,
                                documentType,
                                description
                        );


        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    @GetMapping
    public ResponseEntity<List<MedicalDocumentResponse>>
    getAllDocuments() {

        return ResponseEntity.ok(
                medicalDocumentService
                        .getAllDocuments()
        );
    }


    @GetMapping("/archived")
    public ResponseEntity<List<MedicalDocumentResponse>>
    getArchivedDocuments() {

        return ResponseEntity.ok(
                medicalDocumentService
                        .getArchivedDocuments()
        );
    }


    @GetMapping(
            "/versions/{documentGroupIdOrDocumentId}"
    )
    public ResponseEntity<List<MedicalDocumentResponse>>
    getDocumentVersionHistory(

            @PathVariable
            String documentGroupIdOrDocumentId
    ) {

        return ResponseEntity.ok(
                medicalDocumentService
                        .getDocumentVersionHistory(
                                documentGroupIdOrDocumentId
                        )
        );
    }


    @GetMapping("/{id}")
    public ResponseEntity<MedicalDocumentResponse>
    getDocumentById(
            @PathVariable String id
    ) {

        return ResponseEntity.ok(
                medicalDocumentService
                        .getDocumentById(id)
        );
    }


    @GetMapping(
            "/record/{medicalRecordId}"
    )
    public ResponseEntity<List<MedicalDocumentResponse>>
    getDocumentsByMedicalRecord(
            @PathVariable String medicalRecordId
    ) {

        return ResponseEntity.ok(
                medicalDocumentService
                        .getDocumentsByMedicalRecord(
                                medicalRecordId
                        )
        );
    }


    @GetMapping(
            "/patient/{patientId}"
    )
    public ResponseEntity<List<MedicalDocumentResponse>>
    getDocumentsByPatient(
            @PathVariable String patientId
    ) {

        return ResponseEntity.ok(
                medicalDocumentService
                        .getDocumentsByPatient(
                                patientId
                        )
        );
    }


    @GetMapping(
            "/doctor/{doctorId}"
    )
    public ResponseEntity<List<MedicalDocumentResponse>>
    getDocumentsByDoctor(
            @PathVariable String doctorId
    ) {

        return ResponseEntity.ok(
                medicalDocumentService
                        .getDocumentsByDoctor(
                                doctorId
                        )
        );
    }


    @PutMapping("/{id}")
    public ResponseEntity<MedicalDocumentResponse>
    updateDocumentMetadata(

            @PathVariable String id,

            @RequestBody
            MedicalDocumentUpdateRequest request
    ) {

        return ResponseEntity.ok(
                medicalDocumentService
                        .updateDocumentMetadata(
                                id,
                                request
                        )
        );
    }


    @PutMapping(
            value = "/{id}/replace",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<MedicalDocumentResponse>
    replaceDocumentFile(

            @PathVariable
            String id,

            @RequestParam("file")
            MultipartFile file,

            @RequestParam("doctorId")
            String doctorId,

            @RequestParam(
                    value = "description",
                    required = false
            )
            String description
    ) {

        MedicalDocumentResponse response =
                medicalDocumentService
                        .replaceDocumentFile(
                                id,
                                file,
                                doctorId,
                                description
                        );


        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    /*
     * Normal remove.
     *
     * Keeps:
     * - MongoDB metadata
     * - Cloudinary file
     */
    @PatchMapping("/{id}/archive")
    public ResponseEntity<MedicalDocumentResponse>
    archiveDocument(
            @PathVariable String id
    ) {

        return ResponseEntity.ok(
                medicalDocumentService
                        .archiveDocument(id)
        );
    }


    /*
     * Permanent delete.
     *
     * Only ARCHIVED documents are accepted.
     *
     * Deletes:
     * - Cloudinary actual file
     * - MongoDB metadata
     */
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<Void>
    permanentlyDeleteDocument(
            @PathVariable String id
    ) {

        medicalDocumentService
                .permanentlyDeleteDocument(
                        id
                );


        return ResponseEntity
                .noContent()
                .build();
    }
}