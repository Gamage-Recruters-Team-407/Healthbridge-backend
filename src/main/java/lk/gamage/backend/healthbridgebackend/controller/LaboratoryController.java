package lk.gamage.backend.healthbridgebackend.controller;

import lk.gamage.backend.healthbridgebackend.model.Laboratory;
import lk.gamage.backend.healthbridgebackend.service.LaboratoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/laboratories")
public class LaboratoryController {

    @Autowired
    private LaboratoryService service;

    @GetMapping
    public List<Laboratory> getAllLaboratories() {
        return service.getAllLaboratories();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Laboratory> getLaboratory(@PathVariable String id) {
        return service.getLaboratoryById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Laboratory> updateStatus(@PathVariable String id, @RequestBody java.util.Map<String, String> body) {
        return service.updateLaboratoryStatus(id, body)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Laboratory> createLaboratory(@RequestBody Laboratory laboratory) {
        Laboratory created = service.createLaboratory(laboratory);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Laboratory> updateLaboratory(@PathVariable String id, @RequestBody Laboratory laboratory) {
        return service.updateLaboratory(id, laboratory)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
