package lk.gamage.backend.healthbridgebackend.service;

import lk.gamage.backend.healthbridgebackend.dto.CreateSupportTicketRequest;
import lk.gamage.backend.healthbridgebackend.dto.SupportTicketResponse;
import lk.gamage.backend.healthbridgebackend.mapper.SupportTicketMapper;
import lk.gamage.backend.healthbridgebackend.model.SupportTicket;
import lk.gamage.backend.healthbridgebackend.repository.SupportTicketRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SupportTicketService {

    private final SupportTicketRepository repository;
    private final SupportTicketMapper mapper;

    public SupportTicketService(
            SupportTicketRepository repository,
            SupportTicketMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public SupportTicketResponse createTicket(
        CreateSupportTicketRequest request
) {

    SupportTicket ticket = mapper.toEntity(request);

    LocalDateTime now = LocalDateTime.now();

    ticket.setCreatedAt(now);
    ticket.setUpdatedAt(now);

    SupportTicket savedTicket =
            repository.save(ticket);

    return mapper.toResponse(savedTicket);
}

        public SupportTicketResponse getTicket(String id) {

        SupportTicket ticket = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Support ticket not found: " + id
                        )
                );

        return mapper.toResponse(ticket);
    }

    public List<SupportTicketResponse> getPatientTickets(
            Long patientId
    ) {

        return repository
                .findByPatientIdOrderByCreatedAtDesc(patientId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public List<SupportTicketResponse> getAllTickets() {

        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}