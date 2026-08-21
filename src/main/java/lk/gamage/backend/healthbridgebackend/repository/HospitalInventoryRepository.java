package lk.gamage.backend.healthbridgebackend.repository;

import lk.gamage.backend.healthbridgebackend.model.HospitalInventory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface HospitalInventoryRepository
        extends MongoRepository<HospitalInventory, String> {

    Optional<HospitalInventory> findByItemCode(String itemCode);

    List<HospitalInventory> findByCategory(String category);

    List<HospitalInventory> findByStatus(
            lk.gamage.backend.healthbridgebackend.model.enums.InventoryStatus status);
}