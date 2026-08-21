package lk.gamage.backend.healthbridgebackend.repository;

import lk.gamage.backend.healthbridgebackend.model.SupportTicket;
import lk.gamage.backend.healthbridgebackend.model.TicketStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SupportTicketRepository
        extends MongoRepository<SupportTicket, String> {

    List<SupportTicket> findByPatientId(String patientId);

    List<SupportTicket> findByStatus(TicketStatus status);

    List<SupportTicket> findByPatientIdOrderByCreatedAtDesc(
            String patientId
    );
}