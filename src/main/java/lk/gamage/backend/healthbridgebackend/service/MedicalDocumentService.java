package lk.gamage.backend.healthbridgebackend.service;

import lk.gamage.backend.healthbridgebackend.dto.response.MedicalDocumentResponse;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;


public interface MedicalDocumentService {

    MedicalDocumentResponse uploadDocument(
            MultipartFile file,
            String medicalRecordId,
            String patientId,
            String doctorId,
            String documentType,
            String description
    );


    List<MedicalDocumentResponse> getAllDocuments();


    MedicalDocumentResponse getDocumentById(
            String id
    );


    List<MedicalDocumentResponse> getDocumentsByMedicalRecord(
            String medicalRecordId
    );


    List<MedicalDocumentResponse> getDocumentsByPatient(
            String patientId
    );


    List<MedicalDocumentResponse> getDocumentsByDoctor(
            String doctorId
    );


    void deleteDocument(
            String id
    );
}