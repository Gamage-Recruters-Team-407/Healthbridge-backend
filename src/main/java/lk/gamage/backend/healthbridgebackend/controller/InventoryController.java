package lk.gamage.backend.healthbridgebackend.controller;
import lk.gamage.backend.healthbridgebackend.dto.InventoryRequest;
import lk.gamage.backend.healthbridgebackend.model.HospitalInventory;
import lk.gamage.backend.healthbridgebackend.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hospital-billing/inventory")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    public ResponseEntity<HospitalInventory> create(
            @RequestBody InventoryRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(inventoryService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<HospitalInventory>> getAll() {

        return ResponseEntity.ok(
                inventoryService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HospitalInventory> getById(
            @PathVariable String id) {

        return ResponseEntity.ok(
                inventoryService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HospitalInventory> update(
            @PathVariable String id,
            @RequestBody InventoryRequest request) {

        return ResponseEntity.ok(
                inventoryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable String id) {

        inventoryService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/stock-in/{quantity}")
    public ResponseEntity<HospitalInventory> stockIn(
            @PathVariable String id,
            @PathVariable int quantity) {

        return ResponseEntity.ok(
                inventoryService.stockIn(id, quantity));
    }

    @PatchMapping("/{id}/stock-out/{quantity}")
    public ResponseEntity<HospitalInventory> stockOut(
            @PathVariable String id,
            @PathVariable int quantity) {

        return ResponseEntity.ok(
                inventoryService.stockOut(id, quantity));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<HospitalInventory>>
    lowStock() {

        return ResponseEntity.ok(
                inventoryService.getLowStock());
    }
}
