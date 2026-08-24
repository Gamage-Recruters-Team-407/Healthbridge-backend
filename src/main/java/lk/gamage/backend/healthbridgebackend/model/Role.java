package lk.gamage.backend.healthbridgebackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    @Id
    private String id;
    private String roleId;
    private String name;
    private String type;
    private String status;
    private int userCount;
    private List<String> permissionIds;
    private String riskLevel;
    private String riskRecommendations;
    private LocalDateTime updatedAt;
}
