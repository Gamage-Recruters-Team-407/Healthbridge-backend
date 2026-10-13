package lk.gamage.backend.healthbridgebackend.service.impl;

import jakarta.annotation.PostConstruct;
import lk.gamage.backend.healthbridgebackend.model.Laboratory;
import lk.gamage.backend.healthbridgebackend.repository.LaboratoryRepository;
import lk.gamage.backend.healthbridgebackend.service.LaboratoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LaboratoryServiceImpl implements LaboratoryService {

    private final LaboratoryRepository repository;

    @PostConstruct
    public void seedInitialData() {
        if (repository.count() == 0) {
            Laboratory lab1 = Laboratory.builder()
                    .labId("LB-001")
                    .name("GreenLine Diagnostics")
                    .address("No. 45, Colombo Road, Negombo, Western Province, Sri Lanka")
                    .license("LAB-WP-2291")
                    .owner("S. Perera")
                    .requestDate(LocalDate.now().minusDays(2).toString())
                    .status("Pending")
                    .email("contact@greenline-labs.lk")
                    .phone("+94 77 213 4590")
                    .category("Clinical Pathology & Diagnostics")
                    .yearsInOperation("6 years")
                    .build();

            Laboratory lab2 = Laboratory.builder()
                    .labId("LB-002")
                    .name("MediTest Laboratories")
                    .address("12 Galle Road, Colombo 03, Sri Lanka")
                    .license("LAB-WP-1142")
                    .owner("K. Fernando")
                    .requestDate(LocalDate.now().minusDays(10).toString())
                    .approvedDate(LocalDate.now().minusDays(5).toString())
                    .status("Approved")
                    .email("info@meditest.lk")
                    .phone("+94 11 234 5678")
                    .category("Microbiology & Blood Testing")
                    .yearsInOperation("12 years")
                    .build();

            repository.saveAll(List.of(lab1, lab2));
        }
    }

    @Override
    public List<Laboratory> getAllLaboratories() {
        return repository.findAll();
    }

    @Override
    public Optional<Laboratory> getLaboratoryById(String labId) {
        return repository.findByLabId(labId).or(() -> repository.findById(labId));
    }

    @Override
    public Optional<Laboratory> updateLaboratoryStatus(String labId, Map<String, String> body) {
        return repository.findByLabId(labId).or(() -> repository.findById(labId)).map(lab -> {
            lab.setStatus(body.get("status"));
            if ("Approved".equalsIgnoreCase(body.get("status"))) {
                lab.setApprovedDate(LocalDate.now().toString());
            }
            return repository.save(lab);
        });
    }
    @Override
    public Laboratory createLaboratory(Laboratory laboratory) {
        // Generate a simple labId if not provided
        if (laboratory.getLabId() == null || laboratory.getLabId().isEmpty()) {
            laboratory.setLabId("LB-" + (System.currentTimeMillis() % 100000));
        }
        if (laboratory.getStatus() == null || laboratory.getStatus().isEmpty()) {
            laboratory.setStatus("Pending");
        }
        if (laboratory.getRequestDate() == null || laboratory.getRequestDate().isEmpty()) {
            laboratory.setRequestDate(LocalDate.now().toString());
        }
        return repository.save(laboratory);
    }

    @Override
    public Optional<Laboratory> updateLaboratory(String labId, Laboratory updatedData) {
        return repository.findByLabId(labId).or(() -> repository.findById(labId)).map(lab -> {
            if (updatedData.getName() != null) lab.setName(updatedData.getName());
            if (updatedData.getAddress() != null) lab.setAddress(updatedData.getAddress());
            if (updatedData.getLicense() != null) lab.setLicense(updatedData.getLicense());
            if (updatedData.getOwner() != null) lab.setOwner(updatedData.getOwner());
            if (updatedData.getEmail() != null) lab.setEmail(updatedData.getEmail());
            if (updatedData.getPhone() != null) lab.setPhone(updatedData.getPhone());
            if (updatedData.getCategory() != null) lab.setCategory(updatedData.getCategory());
            if (updatedData.getYearsInOperation() != null) lab.setYearsInOperation(updatedData.getYearsInOperation());
            
            return repository.save(lab);
        });
    }
}
