package com.healthbridge.doctor.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "doctor_leaves")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorLeave {
    @Id private String id;
    private String doctorId;
    private String leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private String status;
}
