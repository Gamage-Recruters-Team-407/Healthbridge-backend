package lk.gamage.backend.healthbridgebackend.mapper;

import lk.gamage.backend.healthbridgebackend.dto.request.CreateSupportTicketRequest;
import lk.gamage.backend.healthbridgebackend.dto.response.SupportTicketResponse;
import lk.gamage.backend.healthbridgebackend.model.SupportTicket;
import lk.gamage.backend.healthbridgebackend.model.TicketStatus;
import org.springframework.stereotype.Component;

@Component
public class SupportTicketMapper {

    public SupportTicket toEntity(
            CreateSupportTicketRequest request
    ) {

        SupportTicket ticket = new SupportTicket();

        // Patient ID and patient name are NOT taken from request.
        // They will be added from the logged-in user in SupportTicketService.

        ticket.setSubject(request.getSubject());
        ticket.setCategory(request.getCategory());
        ticket.setDescription(request.getDescription());
        ticket.setPriority(request.getPriority());
        ticket.setMobileNumber(request.getMobileNumber());

        // New ticket starts as OPEN
        ticket.setStatus(TicketStatus.OPEN);

        return ticket;
    }

    public SupportTicketResponse toResponse(
            SupportTicket ticket
    ) {

        SupportTicketResponse response =
                new SupportTicketResponse();

        response.setId(ticket.getId());

        // Patient information
        response.setPatientId(ticket.getPatientId());
        response.setPatientName(ticket.getPatientName());

        // Ticket information
        response.setSubject(ticket.getSubject());
        response.setCategory(ticket.getCategory());
        response.setDescription(ticket.getDescription());
        response.setPriority(ticket.getPriority());
        response.setMobileNumber(ticket.getMobileNumber());
        response.setStatus(ticket.getStatus());

        // Attachment
        response.setAttachmentUrl(
                ticket.getAttachmentUrl()
        );

        response.setAttachmentPublicId(
                ticket.getAttachmentPublicId()
        );

        // Admin reply information
        response.setAdminReply(
                ticket.getAdminReply()
        );

        response.setRepliedBy(
                ticket.getRepliedBy()
        );

        response.setRepliedAt(
                ticket.getRepliedAt()
        );

        // Dates
        response.setCreatedAt(
                ticket.getCreatedAt()
        );

        response.setUpdatedAt(
                ticket.getUpdatedAt()
        );

        return response;
    }
}