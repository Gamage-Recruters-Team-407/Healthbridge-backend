package com.healthbridge.doctor.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class DoctorRequestDTO {
    @NotBlank @Size(max = 120) private String fullName;
    @NotBlank @Email @Size(max = 180) private String email;
    @NotBlank @Pattern(regexp = "^[+]?[0-9 ()-]{7,20}$") private String phone;
    @NotBlank @Size(max = 120) private String specialization;
    @NotBlank @Size(max = 500) private String qualifications;
    @NotNull @PositiveOrZero private Integer experience;
    @NotNull @DecimalMin(value = "0.0", inclusive = true) private BigDecimal consultationFee;
}
