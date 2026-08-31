package com.healthbridge.doctor.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalTime;

@Document(collection = "doctor_availability")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorAvailability {
    @Id private String id;
    private String doctorId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String status;
}
