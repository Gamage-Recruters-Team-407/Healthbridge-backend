package com.healthbridge.doctor.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "doctors")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Doctor {
    @Id private String id;
    private String userId;
    private String fullName;
    private String email;
    private String phone;
    private String gender;
    private LocalDate dateOfBirth;
    private String address;
    private String profileImage;
    private String specialization;
    private String qualifications;
    private Integer experience;
    private BigDecimal consultationFee;
    private Boolean availableStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
