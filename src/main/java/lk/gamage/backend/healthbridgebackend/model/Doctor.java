package lk.gamage.backend.healthbridgebackend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "doctors")
public class Doctor {

    @Id
    private String id;

    private String name;
    private String doctorId;
    
    private String department;
    private String email;
    
    // Status Fields
    private String accountStatus; // "Active", "Inactive", "Suspended"
    private String branchId;
    private String branchCode;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

