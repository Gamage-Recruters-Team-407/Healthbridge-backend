package lk.gamage.backend.healthbridgebackend.service;

import com.cloudinary.Cloudinary;
import lk.gamage.backend.healthbridgebackend.dto.request.CreateSupportTicketRequest;
import lk.gamage.backend.healthbridgebackend.dto.request.ReplySupportTicketRequest;
import lk.gamage.backend.healthbridgebackend.dto.response.SupportTicketResponse;
import lk.gamage.backend.healthbridgebackend.mapper.SupportTicketMapper;
import lk.gamage.backend.healthbridgebackend.model.SupportTicket;
import lk.gamage.backend.healthbridgebackend.model.TicketStatus;
import lk.gamage.backend.healthbridgebackend.security.CustomUserDetails;
import lk.gamage.backend.healthbridgebackend.repository.SupportTicketRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class SupportTicketService {

    private final SupportTicketRepository repository;
    private final SupportTicketMapper mapper;
    private final CloudinaryService cloudinaryService;

    public SupportTicketService(
            SupportTicketRepository repository,
            SupportTicketMapper mapper,
            CloudinaryService cloudinaryService
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.cloudinaryService = cloudinaryService;
    }

    // ============================================================
    // CREATE TICKET
    // ============================================================

    public SupportTicketResponse createTicket(
            CreateSupportTicketRequest request,
            MultipartFile file
    ) {

        // Get currently logged-in user
        CustomUserDetails user = getLoggedInUser();

        // Convert request to ticket
        SupportTicket ticket = mapper.toEntity(request);

        // IMPORTANT:
        // Patient ID and patient name come from authentication,
        // NOT from the frontend request.
        ticket.setPatientId(user.getId());
        ticket.setPatientName(user.getFullName());

        // Upload attachment if provided
        if (file != null && !file.isEmpty()) {

            Map<String, Object> uploadResult =
                    cloudinaryService.uploadFile(file);

            ticket.setAttachmentUrl(
                    (String) uploadResult.get("secure_url")
            );

            ticket.setAttachmentPublicId(
                    (String) uploadResult.get("public_id")
            );
        }

        LocalDateTime now = LocalDateTime.now();

        ticket.setCreatedAt(now);
        ticket.setUpdatedAt(now);

        SupportTicket savedTicket =
                repository.save(ticket);

        return mapper.toResponse(savedTicket);
    }

    // ============================================================
    // GET SINGLE TICKET
    // ============================================================

    public SupportTicketResponse getTicket(String id) {

        SupportTicket ticket = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Support ticket not found: " + id
                        )
                );

        CustomUserDetails user = getLoggedInUser();

        // Admin can see any ticket
        if (isAdmin(user)) {
            return mapper.toResponse(ticket);
        }

        // Patient can only see their own ticket
        if (!ticket.getPatientId().equals(user.getId())) {
            throw new AccessDeniedException(
                    "You are not allowed to view this ticket"
            );
        }

        return mapper.toResponse(ticket);
    }

    // ============================================================
    // GET MY TICKETS
    // ============================================================

    public List<SupportTicketResponse> getMyTickets() {

        CustomUserDetails user = getLoggedInUser();

        return repository
                .findByPatientIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    // ============================================================
    // GET ALL TICKETS - ADMIN
    // ============================================================

    public List<SupportTicketResponse> getAllTickets() {

        CustomUserDetails user = getLoggedInUser();

        // Only admin can see all tickets
        if (!isAdmin(user)) {
            throw new AccessDeniedException(
                    "Only administrators can view all support tickets"
            );
        }

        return repository
                .findAllByOrderByCreatedAtDesc()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    // ============================================================
    // ADMIN REPLY
    // ============================================================

    public SupportTicketResponse replyToTicket(
            String ticketId,
            ReplySupportTicketRequest request
    ) {

        CustomUserDetails admin = getLoggedInUser();

        // Check admin role
        if (!isAdmin(admin)) {
            throw new AccessDeniedException(
                    "Only administrators can reply to support tickets"
            );
        }

        SupportTicket ticket = repository.findById(ticketId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Support ticket not found: " + ticketId
                        )
                );

        // Store admin reply
        ticket.setAdminReply(request.getReply());

        // Store admin's existing user ID
        ticket.setRepliedBy(admin.getId());

        // Store reply time
        ticket.setRepliedAt(LocalDateTime.now());

        // Update ticket status
        ticket.setStatus(TicketStatus.IN_PROGRESS);

        ticket.setUpdatedAt(LocalDateTime.now());

        SupportTicket updatedTicket =
                repository.save(ticket);

        return mapper.toResponse(updatedTicket);
    }

    // ============================================================
    // UPDATE TICKET STATUS - ADMIN
    // ============================================================

    public SupportTicketResponse updateStatus(
            String ticketId,
            TicketStatus status
    ) {

        CustomUserDetails admin = getLoggedInUser();

        if (!isAdmin(admin)) {
            throw new AccessDeniedException(
                    "Only administrators can update ticket status"
            );
        }

        SupportTicket ticket = repository.findById(ticketId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Support ticket not found: " + ticketId
                        )
                );

        ticket.setStatus(status);
        ticket.setUpdatedAt(LocalDateTime.now());

        SupportTicket updatedTicket =
                repository.save(ticket);

        return mapper.toResponse(updatedTicket);
    }

    // ============================================================
    // GET LOGGED-IN USER
    // ============================================================

    private CustomUserDetails getLoggedInUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new AccessDeniedException(
                    "User is not authenticated"
            );
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof CustomUserDetails)) {

            throw new AccessDeniedException(
                    "Invalid authenticated user"
            );
        }

        return (CustomUserDetails) principal;
    }

    // ============================================================
    // CHECK ADMIN
    // ============================================================

    private boolean isAdmin(CustomUserDetails user) {

        return user.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN") ||
                        authority.getAuthority().equals("ROLE_SUPER_ADMIN")
                );
    }
}