package lk.gamage.backend.healthbridgebackend.service.impl;

import lk.gamage.backend.healthbridgebackend.dto.request.MedicalRecordRequest;
import lk.gamage.backend.healthbridgebackend.dto.response.MedicalRecordResponse;
import lk.gamage.backend.healthbridgebackend.model.MedicalRecord;
import lk.gamage.backend.healthbridgebackend.repository.MedicalRecordRepository;
import lk.gamage.backend.healthbridgebackend.service.MedicalRecordService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class MedicalRecordServiceImpl
        implements MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;

    public MedicalRecordServiceImpl(
            MedicalRecordRepository medicalRecordRepository
    ) {
        this.medicalRecordRepository = medicalRecordRepository;
    }

    @Override
    public MedicalRecordResponse createMedicalRecord(
            MedicalRecordRequest request
    ) {

        MedicalRecord medicalRecord = new MedicalRecord();

        copyRequestToEntity(
                request,
                medicalRecord
        );

        LocalDateTime now = LocalDateTime.now();

        medicalRecord.setCreatedAt(now);
        medicalRecord.setUpdatedAt(now);

        medicalRecord.setVersion(1);

        medicalRecord.setArchived(false);
        medicalRecord.setArchivedAt(null);

        if (
                request.getStatus() == null ||
                request.getStatus().isBlank()
        ) {
            medicalRecord.setStatus("COMPLETED");
        }

        MedicalRecord savedRecord =
                medicalRecordRepository.save(medicalRecord);

        return toResponse(savedRecord);
    }

    @Override
    public List<MedicalRecordResponse> getAllMedicalRecords() {

        return medicalRecordRepository
                .findAllByOrderByVisitDateDesc()
                .stream()
                .filter(this::isActive)
                .map(this::toResponse)
                .toList();
    }

    @Override
    public MedicalRecordResponse getMedicalRecordById(
            String id
    ) {

        MedicalRecord medicalRecord =
                findRecordById(id);

        if (!isActive(medicalRecord)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Medical record not found or archived with id: " + id
            );
        }

        return toResponse(medicalRecord);
    }

    @Override
    public List<MedicalRecordResponse>
    getMedicalRecordsByPatientId(
            String patientId
    ) {

        return medicalRecordRepository
                .findByPatientIdOrderByVisitDateDesc(patientId)
                .stream()
                .filter(this::isActive)
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<MedicalRecordResponse>
    getMedicalRecordsByDoctorId(
            String doctorId
    ) {

        return medicalRecordRepository
                .findByDoctorIdOrderByVisitDateDesc(doctorId)
                .stream()
                .filter(this::isActive)
                .map(this::toResponse)
                .toList();
    }

    @Override
    public MedicalRecordResponse updateMedicalRecord(
            String id,
            MedicalRecordRequest request
    ) {

        MedicalRecord existingRecord =
                findRecordById(id);

        if (!isActive(existingRecord)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Archived medical record cannot be updated. Restore it first."
            );
        }

        copyRequestToEntity(
                request,
                existingRecord
        );

        existingRecord.setUpdatedAt(
                LocalDateTime.now()
        );

        existingRecord.setVersion(
                existingRecord.getVersion() + 1
        );

        if (
                request.getStatus() == null ||
                request.getStatus().isBlank()
        ) {
            existingRecord.setStatus("COMPLETED");
        }

        MedicalRecord updatedRecord =
                medicalRecordRepository.save(existingRecord);

        return toResponse(updatedRecord);
    }

    @Override
    public MedicalRecordResponse archiveMedicalRecord(
            String id
    ) {

        MedicalRecord existingRecord =
                findRecordById(id);

        if (Boolean.TRUE.equals(existingRecord.getArchived())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Medical record is already archived."
            );
        }

        LocalDateTime now = LocalDateTime.now();

        existingRecord.setArchived(true);
        existingRecord.setArchivedAt(now);
        existingRecord.setUpdatedAt(now);

        existingRecord.setVersion(
                existingRecord.getVersion() + 1
        );

        MedicalRecord archivedRecord =
                medicalRecordRepository.save(existingRecord);

        return toResponse(archivedRecord);
    }

    @Override
    public MedicalRecordResponse restoreMedicalRecord(
            String id
    ) {

        MedicalRecord existingRecord =
                findRecordById(id);

        if (!Boolean.TRUE.equals(existingRecord.getArchived())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Medical record is not archived."
            );
        }

        LocalDateTime now = LocalDateTime.now();

        existingRecord.setArchived(false);
        existingRecord.setArchivedAt(null);
        existingRecord.setUpdatedAt(now);

        existingRecord.setVersion(
                existingRecord.getVersion() + 1
        );

        MedicalRecord restoredRecord =
                medicalRecordRepository.save(existingRecord);

        return toResponse(restoredRecord);
    }

    @Override
    public List<MedicalRecordResponse> getArchivedMedicalRecords() {

        return medicalRecordRepository
                .findAllByOrderByVisitDateDesc()
                .stream()
                .filter(record ->
                        Boolean.TRUE.equals(record.getArchived())
                )
                .map(this::toResponse)
                .toList();
    }

    @Override
    public void deleteMedicalRecordPermanently(
            String id
    ) {

        MedicalRecord existingRecord =
                findRecordById(id);

        medicalRecordRepository.delete(existingRecord);
    }

    private boolean isActive(
            MedicalRecord medicalRecord
    ) {

        return !Boolean.TRUE.equals(
                medicalRecord.getArchived()
        );
    }

    private MedicalRecord findRecordById(
            String id
    ) {

        return medicalRecordRepository
                .findById(id)
                .orElseThrow(
                        () ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Medical record not found with id: "
                                                + id
                                )
                );
    }

    private void copyRequestToEntity(
            MedicalRecordRequest request,
            MedicalRecord medicalRecord
    ) {

        medicalRecord.setPatientId(
                request.getPatientId()
        );

        medicalRecord.setDoctorId(
                request.getDoctorId()
        );

        medicalRecord.setDoctorName(
                request.getDoctorName()
        );

        medicalRecord.setHospitalName(
                request.getHospitalName()
        );

        medicalRecord.setVisitDate(
                request.getVisitDate()
        );

        medicalRecord.setRecordType(
                request.getRecordType()
        );

        medicalRecord.setDiagnosis(
                request.getDiagnosis()
        );

        medicalRecord.setClinicalSummary(
                request.getClinicalSummary()
        );

        if (request.getSymptoms() == null) {
            medicalRecord.setSymptoms(
                    new ArrayList<>()
            );
        } else {
            medicalRecord.setSymptoms(
                    new ArrayList<>(
                            request.getSymptoms()
                    )
            );
        }

        if (request.getTreatmentPlan() == null) {
            medicalRecord.setTreatmentPlan(
                    new ArrayList<>()
            );
        } else {
            medicalRecord.setTreatmentPlan(
                    new ArrayList<>(
                            request.getTreatmentPlan()
                    )
            );
        }

        medicalRecord.setConsultationNotes(
                request.getConsultationNotes()
        );

        medicalRecord.setStatus(
                request.getStatus()
        );
    }

    private MedicalRecordResponse toResponse(
            MedicalRecord medicalRecord
    ) {

        MedicalRecordResponse response =
                new MedicalRecordResponse();

        response.setId(
                medicalRecord.getId()
        );

        response.setPatientId(
                medicalRecord.getPatientId()
        );

        response.setDoctorId(
                medicalRecord.getDoctorId()
        );

        response.setDoctorName(
                medicalRecord.getDoctorName()
        );

        response.setHospitalName(
                medicalRecord.getHospitalName()
        );

        response.setVisitDate(
                medicalRecord.getVisitDate()
        );

        response.setRecordType(
                medicalRecord.getRecordType()
        );

        response.setDiagnosis(
                medicalRecord.getDiagnosis()
        );

        response.setClinicalSummary(
                medicalRecord.getClinicalSummary()
        );

        response.setSymptoms(
                medicalRecord.getSymptoms()
        );

        response.setTreatmentPlan(
                medicalRecord.getTreatmentPlan()
        );

        response.setConsultationNotes(
                medicalRecord.getConsultationNotes()
        );

        response.setStatus(
                medicalRecord.getStatus()
        );

        response.setVersion(
                medicalRecord.getVersion()
        );

        response.setCreatedAt(
                medicalRecord.getCreatedAt()
        );

        response.setUpdatedAt(
                medicalRecord.getUpdatedAt()
        );

        response.setArchived(
                Boolean.TRUE.equals(
                        medicalRecord.getArchived()
                )
        );

        response.setArchivedAt(
                medicalRecord.getArchivedAt()
        );

        return response;
    }
}