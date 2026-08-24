package lk.gamage.backend.healthbridgebackend.repository;

import lk.gamage.backend.healthbridgebackend.model.SupportTicket;
import lk.gamage.backend.healthbridgebackend.model.TicketStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportTicketRepository
        extends MongoRepository<SupportTicket, String> {

    // Get all tickets belonging to a specific patient
    List<SupportTicket> findByPatientId(String patientId);

    // Get tickets by status
    List<SupportTicket> findByStatus(TicketStatus status);

    // Patient's tickets, newest first
    List<SupportTicket> findByPatientIdOrderByCreatedAtDesc(
            String patientId
    );

    // All tickets, newest first
    List<SupportTicket> findAllByOrderByCreatedAtDesc();
}