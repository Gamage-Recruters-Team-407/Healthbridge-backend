package lk.gamage.backend.healthbridgebackend.service.impl;


import lk.gamage.backend.healthbridgebackend.dto.request.DiagnosisRequest;
import lk.gamage.backend.healthbridgebackend.dto.response.DiagnosisResponse;
import lk.gamage.backend.healthbridgebackend.model.Diagnosis;
import lk.gamage.backend.healthbridgebackend.repository.DiagnosisRepository;
import lk.gamage.backend.healthbridgebackend.service.DiagnosisService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


import java.util.List;



@Service
public class DiagnosisServiceImpl 
        implements DiagnosisService {


    private final DiagnosisRepository diagnosisRepository;


    public DiagnosisServiceImpl(
            DiagnosisRepository diagnosisRepository
    ) {
        this.diagnosisRepository = diagnosisRepository;
    }



    @Override
    public DiagnosisResponse createDiagnosis(
            DiagnosisRequest request
    ) {


        Diagnosis diagnosis = new Diagnosis();


        mapRequestToEntity(
                request,
                diagnosis
        );


        Diagnosis savedDiagnosis =
                diagnosisRepository.save(diagnosis);


        return mapToResponse(savedDiagnosis);
    }




    @Override
    public List<DiagnosisResponse> getAllDiagnoses() {


        return diagnosisRepository
                .findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();

    }




    @Override
    public DiagnosisResponse getDiagnosisById(
            String id
    ) {


        Diagnosis diagnosis =
                diagnosisRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Diagnosis not found"
                                )
                        );


        return mapToResponse(diagnosis);

    }





    @Override
    public List<DiagnosisResponse>
    getDiagnosesByMedicalRecord(
            String medicalRecordId
    ) {


        return diagnosisRepository
                .findByMedicalRecordId(
                        medicalRecordId
                )
                .stream()
                .map(this::mapToResponse)
                .toList();

    }






    @Override
    public DiagnosisResponse updateDiagnosis(
            String id,
            DiagnosisRequest request
    ) {


        Diagnosis diagnosis =
                diagnosisRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Diagnosis not found"
                                )
                        );


        mapRequestToEntity(
                request,
                diagnosis
        );


        Diagnosis updated =
                diagnosisRepository.save(diagnosis);


        return mapToResponse(updated);

    }






    @Override
    public void deleteDiagnosis(
            String id
    ) {


        Diagnosis diagnosis =
                diagnosisRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Diagnosis not found"
                                )
                        );


        diagnosisRepository.delete(diagnosis);

    }






    private void mapRequestToEntity(
            DiagnosisRequest request,
            Diagnosis diagnosis
    ) {


        diagnosis.setMedicalRecordId(
                request.getMedicalRecordId()
        );


        diagnosis.setPatientId(
                request.getPatientId()
        );


        diagnosis.setDoctorId(
                request.getDoctorId()
        );


        diagnosis.setDiagnosisName(
                request.getDiagnosisName()
        );


        diagnosis.setDescription(
                request.getDescription()
        );


        diagnosis.setSeverity(
                request.getSeverity()
        );


        diagnosis.setDiagnosedDate(
                request.getDiagnosedDate()
        );

    }






    private DiagnosisResponse mapToResponse(
            Diagnosis diagnosis
    ) {


        DiagnosisResponse response =
                new DiagnosisResponse();


        response.setId(
                diagnosis.getId()
        );


        response.setMedicalRecordId(
                diagnosis.getMedicalRecordId()
        );


        response.setPatientId(
                diagnosis.getPatientId()
        );


        response.setDoctorId(
                diagnosis.getDoctorId()
        );


        response.setDiagnosisName(
                diagnosis.getDiagnosisName()
        );


        response.setDescription(
                diagnosis.getDescription()
        );


        response.setSeverity(
                diagnosis.getSeverity()
        );


        response.setDiagnosedDate(
                diagnosis.getDiagnosedDate()
        );


        return response;

    }

}