package lk.gamage.backend.healthbridgebackend.controller;

import lk.gamage.backend.healthbridgebackend.dto.request.TriggerSOSRequest;
import lk.gamage.backend.healthbridgebackend.dto.response.TriggerSOSResponse;
import lk.gamage.backend.healthbridgebackend.model.SOSAlert;
import lk.gamage.backend.healthbridgebackend.service.SOSService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sos")
@RequiredArgsConstructor
public class SOSController {

    private final SOSService sosService;

    @PostMapping("/trigger")
    public ResponseEntity<TriggerSOSResponse> triggerSOS(@RequestBody TriggerSOSRequest request,
                                                         @RequestHeader(value = "X-User-Id", defaultValue = "user-123") String userId) {
        SOSAlert alert = sosService.triggerSOS(userId, request);
        
        TriggerSOSResponse response = TriggerSOSResponse.builder()
                .alertId(alert.getId())
                .status(alert.getStatus())
                .message("Emergency alert sent successfully!")
                .build();
                
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{alertId}/cancel")
    public ResponseEntity<TriggerSOSResponse> cancelSOS(@PathVariable String alertId) {
        SOSAlert alert = sosService.cancelSOS(alertId);
        
        TriggerSOSResponse response = TriggerSOSResponse.builder()
                .alertId(alert.getId())
                .status(alert.getStatus())
                .message("Emergency alert cancelled.")
                .build();
                
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{alertId}")
    public ResponseEntity<SOSAlert> getAlertStatus(@PathVariable String alertId) {
        SOSAlert alert = sosService.getAlert(alertId);
        return ResponseEntity.ok(alert);
    }

    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getUserAlertHistory(@RequestParam String userId) {
        List<SOSAlert> alerts = sosService.getHistory(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("alerts", alerts);
        response.put("total", alerts.size());
        
        return ResponseEntity.ok(response);
    }
}
