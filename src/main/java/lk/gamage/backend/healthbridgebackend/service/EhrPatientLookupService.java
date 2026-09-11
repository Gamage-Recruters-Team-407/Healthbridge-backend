package lk.gamage.backend.healthbridgebackend.service;

import lk.gamage.backend.healthbridgebackend.dto.response.EhrPatientLookupResponse;
import lk.gamage.backend.healthbridgebackend.model.MedicalRecord;
import lk.gamage.backend.healthbridgebackend.model.Role;
import lk.gamage.backend.healthbridgebackend.model.User;
import lk.gamage.backend.healthbridgebackend.repository.MedicalRecordRepository;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class EhrPatientLookupService {

    private static final int MAX_RESULTS = 10;

    private final MongoTemplate mongoTemplate;
    private final MedicalRecordRepository medicalRecordRepository;

    public EhrPatientLookupService(
            MongoTemplate mongoTemplate,
            MedicalRecordRepository medicalRecordRepository
    ) {
        this.mongoTemplate = mongoTemplate;
        this.medicalRecordRepository = medicalRecordRepository;
    }


    /*
     * =========================================================
     * SEARCH REGISTERED PATIENTS
     * =========================================================
     *
     * Used by:
     *
     * 1. Create Medical Record
     * 2. Find Patient EHR
     *
     * Empty query:
     * returns first 10 registered PATIENT users.
     *
     * Search query:
     * searches name / email.
     *
     * Exact MongoDB ID:
     * also supported.
     */
    public List<EhrPatientLookupResponse> searchPatients(
            String searchText
    ) {

        String normalized =
                searchText == null
                        ? ""
                        : searchText.trim();


        /*
         * IMPORTANT:
         *
         * User.role is String.
         * Do NOT compare with:
         *
         * user.getRole() == Role.PATIENT
         *
         * Use String comparison instead.
         */
        Criteria patientRoleCriteria =
                Criteria.where("role")
                        .regex(
                                Pattern.compile(
                                        "^"
                                                + Pattern.quote(Role.PATIENT)
                                                + "$",
                                        Pattern.CASE_INSENSITIVE
                                )
                        );


        /*
         * Empty search.
         *
         * Show a small selectable list.
         */
        if (!StringUtils.hasText(normalized)) {

            Query query =
                    new Query(
                            patientRoleCriteria
                    )
                            .with(
                                    Sort.by(
                                            Sort.Direction.ASC,
                                            "fullName"
                                    )
                            )
                            .limit(
                                    MAX_RESULTS
                            );


            List<User> users =
                    mongoTemplate.find(
                            query,
                            User.class
                    );


            return toResponses(
                    users
            );
        }


        Map<String, User> uniquePatients =
                new LinkedHashMap<>();


        /*
         * First:
         * try exact MongoDB User ID.
         */
        User exactUser =
                mongoTemplate.findById(
                        normalized,
                        User.class
                );


        if (isPatient(exactUser)) {

            uniquePatients.put(
                    exactUser.getId(),
                    exactUser
            );
        }


        /*
         * One character search is too broad.
         *
         * Exact ID result above can still return.
         */
        if (normalized.length() < 2) {

            return toResponses(
                    new ArrayList<>(
                            uniquePatients.values()
                    )
            );
        }


        Pattern searchPattern =
                Pattern.compile(
                        Pattern.quote(
                                normalized
                        ),
                        Pattern.CASE_INSENSITIVE
                );


        Criteria searchCriteria =
                new Criteria()
                        .orOperator(

                                Criteria
                                        .where("fullName")
                                        .regex(
                                                searchPattern
                                        ),

                                Criteria
                                        .where("firstName")
                                        .regex(
                                                searchPattern
                                        ),

                                Criteria
                                        .where("lastName")
                                        .regex(
                                                searchPattern
                                        ),

                                Criteria
                                        .where("email")
                                        .regex(
                                                searchPattern
                                        )
                        );


        Query query =
                new Query(
                        new Criteria()
                                .andOperator(
                                        patientRoleCriteria,
                                        searchCriteria
                                )
                )
                        .with(
                                Sort.by(
                                        Sort.Direction.ASC,
                                        "fullName"
                                )
                        )
                        .limit(
                                MAX_RESULTS
                        );


        List<User> matches =
                mongoTemplate.find(
                        query,
                        User.class
                );


        for (User user : matches) {

            if (!isPatient(user)) {
                continue;
            }


            uniquePatients.putIfAbsent(
                    user.getId(),
                    user
            );


            if (
                    uniquePatients.size()
                            >= MAX_RESULTS
            ) {
                break;
            }
        }


        return toResponses(
                new ArrayList<>(
                        uniquePatients.values()
                )
        );
    }


    /*
     * =========================================================
     * DOCTOR -> MY EHR PATIENTS
     * =========================================================
     *
     * Only patients for whom THIS doctor already
     * created at least one active MedicalRecord.
     *
     * Example:
     *
     * Doctor A
     *   Patient 1 -> 3 records
     *   Patient 2 -> 1 record
     *
     * Doctor B records are NOT shown here.
     */
    public List<EhrPatientLookupResponse> getDoctorPatients(
            String doctorId
    ) {

        if (!StringUtils.hasText(doctorId)) {

            return List.of();
        }


        List<MedicalRecord> doctorRecords =
                medicalRecordRepository
                        .findByDoctorIdOrderByVisitDateDesc(
                                doctorId.trim()
                        );


        Map<String, DoctorPatientSummary> summaries =
                new LinkedHashMap<>();


        for (MedicalRecord record : doctorRecords) {

            if (record == null) {
                continue;
            }


            /*
             * Ignore archived records.
             */
            if (
                    Boolean.TRUE.equals(
                            record.getArchived()
                    )
            ) {
                continue;
            }


            if (
                    !StringUtils.hasText(
                            record.getPatientId()
                    )
            ) {
                continue;
            }


            String patientId =
                    record
                            .getPatientId()
                            .trim();


            DoctorPatientSummary summary =
                    summaries.computeIfAbsent(
                            patientId,
                            ignored ->
                                    new DoctorPatientSummary()
                    );


            summary.recordCount++;


            LocalDate visitDate =
                    record.getVisitDate();


            if (
                    visitDate != null
                            && (
                            summary.lastVisitDate == null
                                    || visitDate.isAfter(
                                    summary.lastVisitDate
                            )
                    )
            ) {

                summary.lastVisitDate =
                        visitDate;
            }
        }


        if (summaries.isEmpty()) {

            return List.of();
        }


        /*
         * Fetch User records for the unique patient IDs.
         */
        Query usersQuery =
                new Query(
                        Criteria
                                .where("_id")
                                .in(
                                        summaries.keySet()
                                )
                );


        List<User> users =
                mongoTemplate.find(
                        usersQuery,
                        User.class
                );


        Map<String, User> usersById =
                new LinkedHashMap<>();


        for (User user : users) {

            if (!isPatient(user)) {
                continue;
            }


            usersById.put(
                    user.getId(),
                    user
            );
        }


        List<EhrPatientLookupResponse> results =
                new ArrayList<>();


        /*
         * summaries is LinkedHashMap.
         *
         * MedicalRecordRepository already returns
         * latest visit first, so this keeps the most
         * recently treated patient near the top.
         */
        for (
                Map.Entry<
                        String,
                        DoctorPatientSummary
                        > entry
                : summaries.entrySet()
        ) {

            String patientId =
                    entry.getKey();


            User patient =
                    usersById.get(
                            patientId
                    );


            if (patient == null) {
                continue;
            }


            DoctorPatientSummary summary =
                    entry.getValue();


            EhrPatientLookupResponse response =
                    toResponse(
                            patient
                    );


            response.setRecordCount(
                    summary.recordCount
            );


            response.setLastVisitDate(
                    summary.lastVisitDate == null
                            ? null
                            : summary
                            .lastVisitDate
                            .toString()
            );


            results.add(
                    response
            );
        }


        return results;
    }


    /*
     * =========================================================
     * HELPERS
     * =========================================================
     */
    private boolean isPatient(
            User user
    ) {

        return user != null

                && StringUtils.hasText(
                user.getId()
        )

                && StringUtils.hasText(
                user.getRole()
        )

                && Role.PATIENT
                .equalsIgnoreCase(
                        user
                                .getRole()
                                .trim()
                );
    }


    private List<EhrPatientLookupResponse> toResponses(
            List<User> users
    ) {

        List<EhrPatientLookupResponse> results =
                new ArrayList<>();


        if (users == null) {
            return results;
        }


        for (User user : users) {

            if (!isPatient(user)) {
                continue;
            }


            results.add(
                    toResponse(
                            user
                    )
            );


            if (
                    results.size()
                            >= MAX_RESULTS
            ) {
                break;
            }
        }


        return results;
    }


    private EhrPatientLookupResponse toResponse(
            User patient
    ) {

        String fullName =
                StringUtils.hasText(
                        patient.getFullName()
                )
                        ? patient
                        .getFullName()
                        .trim()

                        : buildName(
                        patient
                );


        String bloodGroup =
                StringUtils.hasText(
                        patient.getBloodGroup()
                )
                        ? patient
                        .getBloodGroup()
                        .trim()

                        : patient.getBloodType();


        return EhrPatientLookupResponse
                .builder()

                .id(
                        patient.getId()
                )

                .fullName(
                        StringUtils.hasText(
                                fullName
                        )
                                ? fullName
                                : "Patient"
                )

                .dateOfBirth(
                        patient.getDateOfBirth()
                )

                .gender(
                        patient.getGender()
                )

                .bloodGroup(
                        bloodGroup
                )

                .picture(
                        patient.getPicture()
                )

                .build();
    }


    private String buildName(
            User patient
    ) {

        String firstName =
                StringUtils.hasText(
                        patient.getFirstName()
                )
                        ? patient
                        .getFirstName()
                        .trim()
                        : "";


        String lastName =
                StringUtils.hasText(
                        patient.getLastName()
                )
                        ? patient
                        .getLastName()
                        .trim()
                        : "";


        return (
                firstName
                        + " "
                        + lastName
        ).trim();
    }


    private static class DoctorPatientSummary {

        private int recordCount = 0;

        private LocalDate lastVisitDate;
    }
}