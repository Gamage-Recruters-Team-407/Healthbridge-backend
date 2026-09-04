package lk.gamage.backend.healthbridgebackend.service.impl;

import lk.gamage.backend.healthbridgebackend.dto.response.MedicalDocumentResponse;
import lk.gamage.backend.healthbridgebackend.model.MedicalDocument;
import lk.gamage.backend.healthbridgebackend.model.MedicalRecord;
import lk.gamage.backend.healthbridgebackend.repository.MedicalDocumentRepository;
import lk.gamage.backend.healthbridgebackend.repository.MedicalRecordRepository;
import lk.gamage.backend.healthbridgebackend.service.MedicalDocumentCloudinaryService;
import lk.gamage.backend.healthbridgebackend.service.MedicalDocumentService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


@Service
public class MedicalDocumentServiceImpl
        implements MedicalDocumentService {


    private static final long MAX_FILE_SIZE =
            10L * 1024L * 1024L;


    private static final Set<String>
            ALLOWED_CONTENT_TYPES =
            Set.of(
                    "application/pdf",
                    "image/jpeg",
                    "image/png"
            );


    private final MedicalDocumentRepository
            medicalDocumentRepository;

    private final MedicalRecordRepository
            medicalRecordRepository;

    private final MedicalDocumentCloudinaryService
            medicalDocumentCloudinaryService;


    public MedicalDocumentServiceImpl(
            MedicalDocumentRepository medicalDocumentRepository,
            MedicalRecordRepository medicalRecordRepository,
            MedicalDocumentCloudinaryService medicalDocumentCloudinaryService
    ) {

        this.medicalDocumentRepository =
                medicalDocumentRepository;

        this.medicalRecordRepository =
                medicalRecordRepository;

        this.medicalDocumentCloudinaryService =
                medicalDocumentCloudinaryService;
    }


    @Override
    public MedicalDocumentResponse uploadDocument(
            MultipartFile file,
            String medicalRecordId,
            String patientId,
            String doctorId,
            String documentType,
            String description
    ) {

        validateUploadRequest(
                file,
                medicalRecordId,
                patientId,
                doctorId,
                documentType
        );


        MedicalRecord medicalRecord =
                medicalRecordRepository
                        .findById(medicalRecordId)
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Medical record not found"
                                        )
                        );


        if (Boolean.TRUE.equals(
                medicalRecord.getArchived()
        )) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot attach a document to an archived medical record"
            );
        }


        if (!Objects.equals(
                medicalRecord.getPatientId(),
                patientId
        )) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Patient ID does not match the medical record"
            );
        }


        Map<String, String> uploadedFile =
                medicalDocumentCloudinaryService
                        .uploadMedicalDocument(
                                file
                        );


        MedicalDocument medicalDocument =
                new MedicalDocument();


        medicalDocument.setMedicalRecordId(
                medicalRecordId
        );


        medicalDocument.setPatientId(
                patientId
        );


        medicalDocument.setDoctorId(
                doctorId
        );


        medicalDocument.setDocumentType(
                documentType
                        .trim()
                        .toUpperCase(Locale.ROOT)
        );


        medicalDocument.setFileName(
                getSafeFileName(
                        file.getOriginalFilename()
                )
        );


        medicalDocument.setContentType(
                file.getContentType()
        );


        medicalDocument.setFileSize(
                file.getSize()
        );


        medicalDocument.setFileUrl(
                uploadedFile.get("url")
        );


        medicalDocument.setCloudinaryPublicId(
                uploadedFile.get("publicId")
        );


        medicalDocument.setCloudinaryResourceType(
                uploadedFile.get("resourceType")
        );


        medicalDocument.setDescription(
                StringUtils.hasText(description)
                        ? description.trim()
                        : null
        );


        medicalDocument.setUploadedAt(
                LocalDateTime.now()
        );


        try {

            MedicalDocument savedDocument =
                    medicalDocumentRepository.save(
                            medicalDocument
                    );


            return mapToResponse(
                    savedDocument
            );


        } catch (RuntimeException exception) {

            /*
             * If MongoDB saving fails after Cloudinary upload,
             * remove the uploaded Cloudinary resource.
             */
            try {

                medicalDocumentCloudinaryService
                        .deleteMedicalDocument(
                                uploadedFile.get(
                                        "publicId"
                                ),
                                uploadedFile.get(
                                        "resourceType"
                                )
                        );

            } catch (RuntimeException ignored) {

                // Preserve original database exception.
            }


            throw exception;
        }
    }


    @Override
    public List<MedicalDocumentResponse>
    getAllDocuments() {

        return medicalDocumentRepository
                .findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    @Override
    public MedicalDocumentResponse
    getDocumentById(
            String id
    ) {

        return mapToResponse(
                findDocumentById(id)
        );
    }


    @Override
    public List<MedicalDocumentResponse>
    getDocumentsByMedicalRecord(
            String medicalRecordId
    ) {

        return medicalDocumentRepository
                .findByMedicalRecordIdOrderByUploadedAtDesc(
                        medicalRecordId
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    @Override
    public List<MedicalDocumentResponse>
    getDocumentsByPatient(
            String patientId
    ) {

        return medicalDocumentRepository
                .findByPatientIdOrderByUploadedAtDesc(
                        patientId
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    @Override
    public List<MedicalDocumentResponse>
    getDocumentsByDoctor(
            String doctorId
    ) {

        return medicalDocumentRepository
                .findByDoctorIdOrderByUploadedAtDesc(
                        doctorId
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    @Override
    public void deleteDocument(
            String id
    ) {

        MedicalDocument document =
                findDocumentById(id);


        medicalDocumentCloudinaryService
                .deleteMedicalDocument(
                        document
                                .getCloudinaryPublicId(),

                        document
                                .getCloudinaryResourceType()
                );


        medicalDocumentRepository.delete(
                document
        );
    }


    private MedicalDocument findDocumentById(
            String id
    ) {

        return medicalDocumentRepository
                .findById(id)
                .orElseThrow(
                        () ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Medical document not found"
                                )
                );
    }


    private void validateUploadRequest(
            MultipartFile file,
            String medicalRecordId,
            String patientId,
            String doctorId,
            String documentType
    ) {

        if (file == null
                || file.isEmpty()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Medical document file is required"
            );
        }


        if (!StringUtils.hasText(
                medicalRecordId
        )) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Medical record ID is required"
            );
        }


        if (!StringUtils.hasText(
                patientId
        )) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Patient ID is required"
            );
        }


        if (!StringUtils.hasText(
                doctorId
        )) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Doctor ID is required"
            );
        }


        if (!StringUtils.hasText(
                documentType
        )) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Document type is required"
            );
        }


        if (file.getSize() > MAX_FILE_SIZE) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Medical document cannot exceed 10 MB"
            );
        }


        String contentType =
                file.getContentType();


        if (contentType == null
                || !ALLOWED_CONTENT_TYPES
                .contains(
                        contentType
                                .toLowerCase(
                                        Locale.ROOT
                                )
                )) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only PDF, JPEG and PNG files are allowed"
            );
        }
    }


    private String getSafeFileName(
            String originalFileName
    ) {

        if (!StringUtils.hasText(
                originalFileName
        )) {

            return "medical-document";
        }


        try {

            return Paths
                    .get(originalFileName)
                    .getFileName()
                    .toString();

        } catch (Exception exception) {

            return "medical-document";
        }
    }


    private MedicalDocumentResponse
    mapToResponse(
            MedicalDocument document
    ) {

        MedicalDocumentResponse response =
                new MedicalDocumentResponse();


        response.setId(
                document.getId()
        );


        response.setMedicalRecordId(
                document.getMedicalRecordId()
        );


        response.setPatientId(
                document.getPatientId()
        );


        response.setDoctorId(
                document.getDoctorId()
        );


        response.setDocumentType(
                document.getDocumentType()
        );


        response.setFileName(
                document.getFileName()
        );


        response.setContentType(
                document.getContentType()
        );


        response.setFileSize(
                document.getFileSize()
        );


        response.setFileUrl(
                document.getFileUrl()
        );


        response.setDescription(
                document.getDescription()
        );


        response.setUploadedAt(
                document.getUploadedAt()
        );


        return response;
    }
}