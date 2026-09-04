package lk.gamage.backend.healthbridgebackend.dto.response;



import lk.gamage.backend.healthbridgebackend.model.SupportTicket;
import lk.gamage.backend.healthbridgebackend.model.TicketStatus;

import java.time.LocalDateTime;

public class TicketSummaryResponse {

    private String id;
    private String userId;
    private String userName;
    private String subject;
    private TicketStatus status;
    private boolean hasAttachment;
    private int replyCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TicketSummaryResponse() {
    }

    public TicketSummaryResponse(SupportTicket ticket) {
        this.id = ticket.getId();
        this.userId = ticket.getUserId();
        this.userName = ticket.getUserName();
        this.subject = ticket.getSubject();
        this.status = ticket.getStatus();
        this.hasAttachment = ticket.getAttachmentUrl() != null && !ticket.getAttachmentUrl().isBlank();
        this.replyCount = ticket.getReplies() == null ? 0 : ticket.getReplies().size();
        this.createdAt = ticket.getCreatedAt();
        this.updatedAt = ticket.getUpdatedAt();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }

    public boolean isHasAttachment() { return hasAttachment; }
    public void setHasAttachment(boolean hasAttachment) { this.hasAttachment = hasAttachment; }

    public int getReplyCount() { return replyCount; }
    public void setReplyCount(int replyCount) { this.replyCount = replyCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}