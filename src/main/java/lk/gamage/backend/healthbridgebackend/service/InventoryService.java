package lk.gamage.backend.healthbridgebackend.service;

import lk.gamage.backend.healthbridgebackend.dto.InventoryRequest;
import lk.gamage.backend.healthbridgebackend.model.HospitalInventory;

import java.util.List;

public interface InventoryService {

    HospitalInventory create(InventoryRequest request);

    HospitalInventory getById(String id);

    List<HospitalInventory> getAll();

    HospitalInventory update(
            String id,
            InventoryRequest request);

    void delete(String id);

    HospitalInventory stockIn(
            String id,
            int quantity);

    HospitalInventory stockOut(
            String id,
            int quantity);

    List<HospitalInventory> getLowStock();
}
