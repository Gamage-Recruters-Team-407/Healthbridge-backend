package lk.gamage.backend.healthbridgebackend.dto.response;

import lk.gamage.backend.healthbridgebackend.model.SupportTicket;
import lk.gamage.backend.healthbridgebackend.model.TicketStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class TicketResponse {

    private String id;
    private String userId;
    private String userName;
    private String userEmail;
    private String subject;
    private String description;
    private String attachmentUrl;
    private TicketStatus status;
    private List<TicketReplyResponse> replies;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TicketResponse() {
    }

    public TicketResponse(SupportTicket ticket) {
        this.id = ticket.getId();
        this.userId = ticket.getUserId();
        this.userName = ticket.getUserName();
        this.userEmail = ticket.getUserEmail();
        this.subject = ticket.getSubject();
        this.description = ticket.getDescription();
        this.attachmentUrl = ticket.getAttachmentUrl();
        this.status = ticket.getStatus();
        this.replies = ticket.getReplies() == null ? List.of() :
                ticket.getReplies().stream().map(TicketReplyResponse::new).collect(Collectors.toList());
        this.createdAt = ticket.getCreatedAt();
        this.updatedAt = ticket.getUpdatedAt();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAttachmentUrl() { return attachmentUrl; }
    public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }

    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }

    public List<TicketReplyResponse> getReplies() { return replies; }
    public void setReplies(List<TicketReplyResponse> replies) { this.replies = replies; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}