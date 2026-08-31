package lk.gamage.backend.healthbridgebackend.service.impl;


import lk.gamage.backend.healthbridgebackend.dto.request.TreatmentRecordRequest;
import lk.gamage.backend.healthbridgebackend.dto.response.TreatmentRecordResponse;
import lk.gamage.backend.healthbridgebackend.model.TreatmentRecord;
import lk.gamage.backend.healthbridgebackend.repository.TreatmentRecordRepository;
import lk.gamage.backend.healthbridgebackend.service.TreatmentRecordService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


import java.util.List;



@Service
public class TreatmentRecordServiceImpl
        implements TreatmentRecordService {



    private final TreatmentRecordRepository treatmentRecordRepository;



    public TreatmentRecordServiceImpl(
            TreatmentRecordRepository treatmentRecordRepository
    ) {
        this.treatmentRecordRepository = treatmentRecordRepository;
    }





    @Override
    public TreatmentRecordResponse createTreatment(
            TreatmentRecordRequest request
    ) {


        TreatmentRecord treatmentRecord =
                new TreatmentRecord();


        mapRequestToEntity(
                request,
                treatmentRecord
        );


        TreatmentRecord savedTreatment =
                treatmentRecordRepository.save(
                        treatmentRecord
                );


        return mapToResponse(savedTreatment);

    }






    @Override
    public List<TreatmentRecordResponse> getAllTreatments() {


        return treatmentRecordRepository
                .findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();

    }







    @Override
    public TreatmentRecordResponse getTreatmentById(
            String id
    ) {


        TreatmentRecord treatmentRecord =
                treatmentRecordRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Treatment record not found"
                                )
                        );


        return mapToResponse(treatmentRecord);

    }







    @Override
    public List<TreatmentRecordResponse>
    getTreatmentsByMedicalRecord(
            String medicalRecordId
    ) {


        return treatmentRecordRepository
                .findByMedicalRecordId(
                        medicalRecordId
                )
                .stream()
                .map(this::mapToResponse)
                .toList();

    }







    @Override
    public TreatmentRecordResponse updateTreatment(
            String id,
            TreatmentRecordRequest request
    ) {


        TreatmentRecord existingTreatment =
                treatmentRecordRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Treatment record not found"
                                )
                        );


        mapRequestToEntity(
                request,
                existingTreatment
        );


        TreatmentRecord updatedTreatment =
                treatmentRecordRepository.save(
                        existingTreatment
                );


        return mapToResponse(updatedTreatment);

    }







    @Override
    public void deleteTreatment(
            String id
    ) {


        TreatmentRecord treatmentRecord =
                treatmentRecordRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Treatment record not found"
                                )
                        );


        treatmentRecordRepository.delete(
                treatmentRecord
        );

    }







    private void mapRequestToEntity(
            TreatmentRecordRequest request,
            TreatmentRecord treatmentRecord
    ) {


        treatmentRecord.setMedicalRecordId(
                request.getMedicalRecordId()
        );


        treatmentRecord.setPatientId(
                request.getPatientId()
        );


        treatmentRecord.setDoctorId(
                request.getDoctorId()
        );


        treatmentRecord.setTreatmentType(
                request.getTreatmentType()
        );


        treatmentRecord.setDescription(
                request.getDescription()
        );


        treatmentRecord.setStartDate(
                request.getStartDate()
        );


        treatmentRecord.setEndDate(
                request.getEndDate()
        );


        treatmentRecord.setStatus(
                request.getStatus()
        );

    }








    private TreatmentRecordResponse mapToResponse(
            TreatmentRecord treatmentRecord
    ) {


        TreatmentRecordResponse response =
                new TreatmentRecordResponse();


        response.setId(
                treatmentRecord.getId()
        );


        response.setMedicalRecordId(
                treatmentRecord.getMedicalRecordId()
        );


        response.setPatientId(
                treatmentRecord.getPatientId()
        );


        response.setDoctorId(
                treatmentRecord.getDoctorId()
        );


        response.setTreatmentType(
                treatmentRecord.getTreatmentType()
        );


        response.setDescription(
                treatmentRecord.getDescription()
        );


        response.setStartDate(
                treatmentRecord.getStartDate()
        );


        response.setEndDate(
                treatmentRecord.getEndDate()
        );


        response.setStatus(
                treatmentRecord.getStatus()
        );


        return response;

    }

}