package lk.gamage.backend.healthbridgebackend.controller;

import lk.gamage.backend.healthbridgebackend.service.SupportTicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
public class SupportTicketController {

    @Autowired
    private SupportTicketService supportTicketService;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> createTicket(
            @RequestParam("subject") String subject,
            @RequestParam("description") String description,
            @RequestParam(value = "attachment", required = false) MultipartFile attachment) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(supportTicketService.createTicket(subject, description, attachment));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error creating ticket: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getMyTickets() {
        try {
            return ResponseEntity.ok(supportTicketService.getMyTickets());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error retrieving tickets: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getMyTicketById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(supportTicketService.getMyTicketById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error retrieving ticket: " + e.getMessage()));
        }
    }

    @PostMapping(value = "/{id}/reply", consumes = "multipart/form-data")
    public ResponseEntity<?> reply(
            @PathVariable String id,
            @RequestParam(value = "message", required = false) String message,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        try {
            return ResponseEntity.ok(supportTicketService.addUserReply(id, message, image));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error adding reply: " + e.getMessage()));
        }
    }
}