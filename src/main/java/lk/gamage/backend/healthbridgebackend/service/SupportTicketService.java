package lk.gamage.backend.healthbridgebackend.service;

import com.cloudinary.Cloudinary;
import lk.gamage.backend.healthbridgebackend.dto.request.CreateSupportTicketRequest;
import lk.gamage.backend.healthbridgebackend.dto.response.SupportTicketResponse;
import lk.gamage.backend.healthbridgebackend.mapper.SupportTicketMapper;
import lk.gamage.backend.healthbridgebackend.model.SupportTicket;
import lk.gamage.backend.healthbridgebackend.repository.SupportTicketRepository;
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

    public SupportTicketResponse createTicket(
            CreateSupportTicketRequest request,
            MultipartFile file
    ) {

        SupportTicket ticket = mapper.toEntity(request);

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
        String patientId
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