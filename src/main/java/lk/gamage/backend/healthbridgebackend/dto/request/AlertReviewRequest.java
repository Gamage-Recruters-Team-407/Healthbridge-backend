package lk.gamage.backend.healthbridgebackend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertReviewRequest {
    
    @NotBlank(message = "Status is required")
    private String status;  // CONFIRMED_FRAUD, FALSE_POSITIVE, ESCALATED
    
    private String reviewNotes;
}
