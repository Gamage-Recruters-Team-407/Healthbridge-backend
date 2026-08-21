package lk.gamage.backend.healthbridgebackend.controller;

import jakarta.validation.Valid;
import lk.gamage.backend.healthbridgebackend.dto.request.CreateSupportTicketRequest;
import lk.gamage.backend.healthbridgebackend.dto.response.SupportTicketResponse;
import lk.gamage.backend.healthbridgebackend.service.SupportTicketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/support/tickets")
public class SupportTicketController {

    private final SupportTicketService service;

    public SupportTicketController(
            SupportTicketService service
    ) {
        this.service = service;
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<SupportTicketResponse> createTicket(
            @Valid @ModelAttribute CreateSupportTicketRequest request,
            @RequestPart(
                    value = "file",
                    required = false
            ) MultipartFile file
    ) {

        SupportTicketResponse response =
                service.createTicket(request, file);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupportTicketResponse> getTicket(
            @PathVariable String id
    ) {

        return ResponseEntity.ok(
                service.getTicket(id)
        );
    }

 @GetMapping("/patient/{patientId}")
public ResponseEntity<List<SupportTicketResponse>> getPatientTickets(
        @PathVariable String patientId
) {

    return ResponseEntity.ok(
            service.getPatientTickets(patientId)
    );
}

    @GetMapping
    public ResponseEntity<List<SupportTicketResponse>> getAllTickets() {

        return ResponseEntity.ok(
                service.getAllTickets()
        );
    }
}