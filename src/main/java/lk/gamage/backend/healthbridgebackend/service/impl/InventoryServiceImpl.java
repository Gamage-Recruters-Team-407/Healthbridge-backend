package lk.gamage.backend.healthbridgebackend.service.impl;
import lk.gamage.backend.healthbridgebackend.dto.InventoryRequest;
import lk.gamage.backend.healthbridgebackend.model.HospitalInventory;
import lk.gamage.backend.healthbridgebackend.model.enums.InventoryStatus;
import lk.gamage.backend.healthbridgebackend.repository.HospitalInventoryRepository;
import lk.gamage.backend.healthbridgebackend.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl
        implements InventoryService {

    private final HospitalInventoryRepository repository;

    @Override
    public HospitalInventory create(
            InventoryRequest request) {

        HospitalInventory inventory =
                HospitalInventory.builder()
                        .itemCode(request.getItemCode())
                        .itemName(request.getItemName())
                        .category(request.getCategory())
                        .supplier(request.getSupplier())
                        .quantity(request.getQuantity())
                        .minimumStock(request.getMinimumStock())
                        .unit(request.getUnit())
                        .unitCost(request.getUnitCost())
                        .expiryDate(request.getExpiryDate())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

        updateStatus(inventory);

        return repository.save(inventory);
    }

    @Override
    public HospitalInventory getById(String id) {

        return repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Inventory item not found"));
    }

    @Override
    public List<HospitalInventory> getAll() {

        return repository.findAll();
    }

    @Override
    public HospitalInventory update(
            String id,
            InventoryRequest request) {

        HospitalInventory item = getById(id);

        item.setItemName(request.getItemName());
        item.setCategory(request.getCategory());
        item.setSupplier(request.getSupplier());
        item.setQuantity(request.getQuantity());
        item.setMinimumStock(request.getMinimumStock());
        item.setUnit(request.getUnit());
        item.setUnitCost(request.getUnitCost());
        item.setExpiryDate(request.getExpiryDate());
        item.setUpdatedAt(LocalDateTime.now());

        updateStatus(item);

        return repository.save(item);
    }

    @Override
    public void delete(String id) {

        repository.deleteById(id);
    }

    @Override
    public HospitalInventory stockIn(
            String id,
            int quantity) {

        HospitalInventory item = getById(id);

        item.setQuantity(
                item.getQuantity() + quantity);

        item.setUpdatedAt(LocalDateTime.now());

        updateStatus(item);

        return repository.save(item);
    }

    @Override
    public HospitalInventory stockOut(
            String id,
            int quantity) {

        HospitalInventory item = getById(id);

        if (item.getQuantity() < quantity) {
            throw new RuntimeException(
                    "Insufficient stock");
        }

        item.setQuantity(
                item.getQuantity() - quantity);

        item.setUpdatedAt(LocalDateTime.now());

        updateStatus(item);

        return repository.save(item);
    }

    @Override
    public List<HospitalInventory> getLowStock() {

        return repository.findByStatus(
                InventoryStatus.LOW_STOCK);
    }

    private void updateStatus(
            HospitalInventory item) {

        if (item.getQuantity() <= 0) {

            item.setStatus(
                    InventoryStatus.OUT_OF_STOCK);

        } else if (
                item.getQuantity()
                        <= item.getMinimumStock()) {

            item.setStatus(
                    InventoryStatus.LOW_STOCK);

        } else {

            item.setStatus(
                    InventoryStatus.IN_STOCK);
        }
    }
}
