package lk.gamage.backend.healthbridgebackend.controller;

import lk.gamage.backend.healthbridgebackend.model.Branch;
import lk.gamage.backend.healthbridgebackend.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/api/hospitals")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class HospitalController {

    private final BranchRepository branchRepository; // 👈 BranchRepository waparann

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllHospitals() {
        log.info("📥 GET /api/hospitals - Fetching branches as hospitals");

        // Branch data tika frontend ekata hospital select ekata match wena widihata map karanawa
        List<Map<String, Object>> hospitalList = branchRepository.findAll().stream().map(branch -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", branch.getId());
            map.put("name", branch.getBranchName() + " (" + branch.getCity() + ")");
            map.put("address", branch.getAddress());
            map.put("phone", branch.getPhone());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(hospitalList);
    }
}