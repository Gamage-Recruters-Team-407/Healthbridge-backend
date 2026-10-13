package lk.gamage.backend.healthbridgebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorResponseDto {
    private String id;
    private String doctorId;
    private String name;
    private String department;
    private String email;
    private String accountStatus;
    private String branchId;
    private String branchCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
