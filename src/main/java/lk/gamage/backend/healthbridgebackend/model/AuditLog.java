package lk.gamage.backend.healthbridgebackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "audit_logs")
public class AuditLog {
    @Id
    private String id;
    private String userId;
    private String userRole;
    private String eventCategory;
    private String actionDescription;
    private String ipAddress;
    private String status;
    private LocalDateTime timestamp;
}
