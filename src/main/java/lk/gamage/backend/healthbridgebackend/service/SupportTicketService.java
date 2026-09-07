package lk.gamage.backend.healthbridgebackend.service;

import lk.gamage.backend.healthbridgebackend.dto.response.TicketResponse;
import lk.gamage.backend.healthbridgebackend.dto.response.TicketSummaryResponse;
import lk.gamage.backend.healthbridgebackend.model.SupportTicket;
import lk.gamage.backend.healthbridgebackend.model.TicketReply;
import lk.gamage.backend.healthbridgebackend.model.TicketCategory;
import lk.gamage.backend.healthbridgebackend.model.TicketStatus;
import lk.gamage.backend.healthbridgebackend.model.User;
import lk.gamage.backend.healthbridgebackend.repository.SupportTicketRepository;
import lk.gamage.backend.healthbridgebackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SupportTicketService {

    @Autowired
    private SupportTicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
    }

    public TicketResponse createTicket(String subject, String description, TicketCategory category,
                                       String contactNumber, MultipartFile attachment) {
        User user = getCurrentUser();

        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("Subject is required");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description is required");
        }
        if (category == null) {
            throw new IllegalArgumentException("Category is required");
        }
        if (contactNumber == null || contactNumber.isBlank()) {
            throw new IllegalArgumentException("Contact number is required");
        }

        SupportTicket.SupportTicketBuilder builder = SupportTicket.builder()
                .userId(user.getId())
                .userName(user.getFullName())
                .userEmail(user.getEmail())
                .subject(subject.trim())
                .description(description.trim())
                .category(category)
                .contactNumber(contactNumber.trim())
                .status(TicketStatus.OPEN);

        if (attachment != null && !attachment.isEmpty()) {
            Map<String, String> uploaded = cloudinaryService.uploadFile(attachment, "support-tickets/attachments");
            builder.attachmentUrl(uploaded.get("url"))
                   .attachmentPublicId(uploaded.get("publicId"));
        }

        SupportTicket saved = ticketRepository.save(builder.build());
        return new TicketResponse(saved);
    }

    public List<TicketSummaryResponse> getMyTickets() {
        User user = getCurrentUser();
        return ticketRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream().map(TicketSummaryResponse::new).collect(Collectors.toList());
    }

    public TicketResponse getMyTicketById(String ticketId) {
        User user = getCurrentUser();
        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));

        if (!ticket.getUserId().equals(user.getId())) {
            throw new SecurityException("You are not allowed to view this ticket");
        }
        return new TicketResponse(ticket);
    }

    public TicketResponse addUserReply(String ticketId, String message, MultipartFile image) {
        User user = getCurrentUser();
        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));

        if (!ticket.getUserId().equals(user.getId())) {
            throw new SecurityException("You are not allowed to reply to this ticket");
        }

        TicketReply reply = buildReply(user.getId(), user.getFullName(), "USER", message, image);
        ticket.getReplies().add(reply);
        ticket.setUpdatedAt(LocalDateTime.now());

        return new TicketResponse(ticketRepository.save(ticket));
    }

    public TicketResponse editUserReply(String ticketId, String replyId, String message) {
        return editReply(ticketId, replyId, message, "USER");
    }

    public TicketResponse deleteUserReply(String ticketId, String replyId) {
        return deleteReply(ticketId, replyId, "USER");
    }

    // ---------- Admin ----------

    public List<TicketSummaryResponse> getAllTickets() {
        return ticketRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(TicketSummaryResponse::new).collect(Collectors.toList());
    }

    public TicketResponse getTicketByIdForAdmin(String ticketId) {
        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));
        return new TicketResponse(ticket);
    }

    public TicketResponse updateStatus(String ticketId, TicketStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status is required");
        }
        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));

        ticket.setStatus(status);
        ticket.setUpdatedAt(LocalDateTime.now());
        return new TicketResponse(ticketRepository.save(ticket));
    }

    public TicketResponse addAdminReply(String ticketId, String message, MultipartFile image) {
        User admin = getCurrentUser();
        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));

        TicketReply reply = buildReply(admin.getId(), admin.getFullName(), "ADMIN", message, image);
        ticket.getReplies().add(reply);

        // Auto move OPEN -> PROCESSING when admin engages, if still open
        if (ticket.getStatus() == TicketStatus.OPEN) {
            ticket.setStatus(TicketStatus.PROCESSING);
        }
        ticket.setUpdatedAt(LocalDateTime.now());

        return new TicketResponse(ticketRepository.save(ticket));
    }

    public TicketResponse editAdminReply(String ticketId, String replyId, String message) {
        return editReply(ticketId, replyId, message, "ADMIN");
    }

    public TicketResponse deleteAdminReply(String ticketId, String replyId) {
        return deleteReply(ticketId, replyId, "ADMIN");
    }

    private TicketResponse editReply(String ticketId, String replyId, String message, String senderRole) {
        User user = getCurrentUser();
        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));
        TicketReply reply = findReplyForSender(ticket, replyId, user, senderRole);

        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message is required");
        }

        reply.setMessage(message.trim());
        ticket.setUpdatedAt(LocalDateTime.now());
        return new TicketResponse(ticketRepository.save(ticket));
    }

    private TicketResponse deleteReply(String ticketId, String replyId, String senderRole) {
        User user = getCurrentUser();
        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));
        TicketReply reply = findReplyForSender(ticket, replyId, user, senderRole);

        ticket.getReplies().remove(reply);
        ticket.setUpdatedAt(LocalDateTime.now());
        return new TicketResponse(ticketRepository.save(ticket));
    }

    private TicketReply findReplyForSender(SupportTicket ticket, String replyId, User user, String senderRole) {
        TicketReply reply = ticket.getReplies().stream()
            .filter(candidate -> Objects.equals(candidate.getId(), replyId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Reply not found"));

        if (!user.getId().equals(reply.getSenderId()) || !senderRole.equals(reply.getSenderRole())) {
            throw new SecurityException("You are not allowed to modify this reply");
        }
        return reply;
    }

    private TicketReply buildReply(String senderId, String senderName, String senderRole,
                                    String message, MultipartFile image) {
        if ((message == null || message.isBlank()) && (image == null || image.isEmpty())) {
            throw new IllegalArgumentException("Reply must contain a message or an image");
        }

        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            Map<String, String> uploaded = cloudinaryService.uploadFile(image, "support-tickets/replies");
            imageUrl = uploaded.get("url");
        }

        return TicketReply.builder()
                .id(UUID.randomUUID().toString())
                .senderId(senderId)
                .senderName(senderName)
                .senderRole(senderRole)
                .message(message == null ? null : message.trim())
                .imageUrl(imageUrl)
                .createdAt(LocalDateTime.now())
                .build();
    }
}