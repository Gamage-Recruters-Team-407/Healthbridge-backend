package com.healthbridge.doctor.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class LeaveRequestDTO {
    @NotBlank private String doctorId;
    @NotBlank @Size(max = 50) private String leaveType;
    @NotNull private LocalDate startDate;
    @NotNull private LocalDate endDate;
    @NotBlank @Size(max = 500) private String reason;
}
