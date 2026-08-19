package lk.gamage.backend.healthbridgebackend.mapper;

import lk.gamage.backend.healthbridgebackend.dto.CreateSupportTicketRequest;
import lk.gamage.backend.healthbridgebackend.dto.SupportTicketResponse;
import lk.gamage.backend.healthbridgebackend.model.SupportTicket;
import lk.gamage.backend.healthbridgebackend.model.TicketStatus;
import org.springframework.stereotype.Component;

@Component
public class SupportTicketMapper {

    public SupportTicket toEntity(CreateSupportTicketRequest request) {

        SupportTicket ticket = new SupportTicket();

        ticket.setPatientId(request.getPatientId());
        ticket.setSubject(request.getSubject());
        ticket.setCategory(request.getCategory());
        ticket.setDescription(request.getDescription());
        ticket.setPriority(request.getPriority());
        ticket.setPreferredContactMethod(
                request.getPreferredContactMethod()
        );
        ticket.setStatus(TicketStatus.OPEN);

        return ticket;
    }

    public SupportTicketResponse toResponse(SupportTicket ticket) {

        SupportTicketResponse response =
                new SupportTicketResponse();

        response.setId(ticket.getId());
        response.setPatientId(ticket.getPatientId());
        response.setSubject(ticket.getSubject());
        response.setCategory(ticket.getCategory());
        response.setDescription(ticket.getDescription());
        response.setPriority(ticket.getPriority());
        response.setPreferredContactMethod(
                ticket.getPreferredContactMethod()
        );
        response.setStatus(ticket.getStatus());
        response.setCreatedAt(ticket.getCreatedAt());
        response.setUpdatedAt(ticket.getUpdatedAt());

        return response;
    }
}