package lk.gamage.backend.healthbridgebackend.controller;

import jakarta.validation.Valid;
import lk.gamage.backend.healthbridgebackend.dto.request.CreateSupportTicketRequest;
import lk.gamage.backend.healthbridgebackend.dto.request.ReplySupportTicketRequest;
import lk.gamage.backend.healthbridgebackend.dto.response.SupportTicketResponse;
import lk.gamage.backend.healthbridgebackend.model.TicketStatus;
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

    // ============================================================
    // PATIENT - CREATE TICKET
    // ============================================================

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

    // ============================================================
    // PATIENT - GET MY TICKETS
    // ============================================================

    @GetMapping("/my")
    public ResponseEntity<List<SupportTicketResponse>> getMyTickets() {

        return ResponseEntity.ok(
                service.getMyTickets()
        );
    }

    // ============================================================
    // GET SINGLE TICKET
    // PATIENT -> OWN TICKET
    // ADMIN  -> ANY TICKET
    // ============================================================

    @GetMapping("/{id}")
    public ResponseEntity<SupportTicketResponse> getTicket(
            @PathVariable String id
    ) {

        return ResponseEntity.ok(
                service.getTicket(id)
        );
    }

    // ============================================================
    // ADMIN - GET ALL TICKETS
    // ============================================================

    @GetMapping
    public ResponseEntity<List<SupportTicketResponse>> getAllTickets() {

        return ResponseEntity.ok(
                service.getAllTickets()
        );
    }

    // ============================================================
    // ADMIN - REPLY TO TICKET
    // ============================================================

    @PutMapping("/{id}/reply")
    public ResponseEntity<SupportTicketResponse> replyToTicket(

            @PathVariable String id,

            @Valid @RequestBody ReplySupportTicketRequest request

    ) {

        return ResponseEntity.ok(
                service.replyToTicket(id, request)
        );
    }

    // ============================================================
    // ADMIN - UPDATE TICKET STATUS
    // ============================================================

    @PutMapping("/{id}/status")
    public ResponseEntity<SupportTicketResponse> updateStatus(

            @PathVariable String id,

            @RequestParam TicketStatus status

    ) {

        return ResponseEntity.ok(
                service.updateStatus(id, status)
        );
    }
}