package lk.gamage.backend.healthbridgebackend.controller;

import lk.gamage.backend.healthbridgebackend.dto.response.MedicalRecordResponse;
import lk.gamage.backend.healthbridgebackend.model.Role;
import lk.gamage.backend.healthbridgebackend.repository.UserRepository;
import lk.gamage.backend.healthbridgebackend.security.CustomUserDetails;
import lk.gamage.backend.healthbridgebackend.service.MedicalRecordService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/medical-records")
public class DoctorEhrPatientsController {
    private final MedicalRecordService medicalRecordService;
    private final UserRepository userRepository;

    public DoctorEhrPatientsController(MedicalRecordService medicalRecordService,
                                      UserRepository userRepository) {
        this.medicalRecordService = medicalRecordService;
        this.userRepository = userRepository;
    }

    @GetMapping("/my-patients")
    @PreAuthorize("hasRole('DOCTOR')")
    public List<PatientLookupResponse> getMyPatients(Authentication authentication) {
        CustomUserDetails doctor = (CustomUserDetails) authentication.getPrincipal();
        var recordsByPatient = medicalRecordService.getMedicalRecordsByDoctorId(doctor.getId())
                .stream()
                .collect(Collectors.groupingBy(MedicalRecordResponse::getPatientId));

        if (recordsByPatient.isEmpty()) {
            return List.of();
        }

        return userRepository.findAllById(recordsByPatient.keySet()).stream()
                .filter(patient -> Role.PATIENT.equals(patient.getRole()))
                .map(patient -> {
                    var records = recordsByPatient.get(patient.getId());
                    LocalDate lastVisitDate = records.stream()
                            .map(MedicalRecordResponse::getVisitDate)
                            .filter(Objects::nonNull)
                            .max(Comparator.naturalOrder())
                            .orElse(null);
                    return new PatientLookupResponse(patient.getId(), patient.getFullName(),
                            patient.getDateOfBirth(), patient.getGender(), patient.getBloodGroup(),
                            patient.getPicture(), lastVisitDate, records.size());
                })
                .sorted(Comparator.comparing(PatientLookupResponse::lastVisitDate,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    public record PatientLookupResponse(String id, String fullName, String dateOfBirth,
                                        String gender, String bloodGroup, String picture,
                                        LocalDate lastVisitDate, int recordCount) {
    }
}
