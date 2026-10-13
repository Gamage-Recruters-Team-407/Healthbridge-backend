package lk.gamage.backend.healthbridgebackend.service;

import lk.gamage.backend.healthbridgebackend.model.Laboratory;
import java.util.List;
import java.util.Optional;
import java.util.Map;

public interface LaboratoryService {
    List<Laboratory> getAllLaboratories();
    Optional<Laboratory> getLaboratoryById(String labId);
    Optional<Laboratory> updateLaboratoryStatus(String labId, Map<String, String> body);
    Laboratory createLaboratory(Laboratory laboratory);
    Optional<Laboratory> updateLaboratory(String labId, Laboratory laboratory);
}
