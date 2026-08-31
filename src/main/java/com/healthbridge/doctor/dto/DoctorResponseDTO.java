package com.healthbridge.doctor.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorResponseDTO {
    private String id;
    private String fullName;
    private String email;
    private String specialization;
    private String qualifications;
    private Integer experience;
    private BigDecimal consultationFee;
}
