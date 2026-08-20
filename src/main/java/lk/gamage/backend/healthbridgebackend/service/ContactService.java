package lk.gamage.backend.healthbridgebackend.service;

import lk.gamage.backend.healthbridgebackend.dto.request.AddContactRequest;
import lk.gamage.backend.healthbridgebackend.model.EmergencyContact;
import lk.gamage.backend.healthbridgebackend.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactRepository contactRepository;

    public List<EmergencyContact> getContacts(String userId) {
        return contactRepository.findByUserId(userId);
    }

    public EmergencyContact addContact(String userId, AddContactRequest request) {
        EmergencyContact contact = EmergencyContact.builder()
                .userId(userId)
                .name(request.getName())
                .relationship(request.getRelationship())
                .phone(request.getPhone())
                .email(request.getEmail())
                .isPrimary(request.isPrimary())
                .createdAt(Instant.now())
                .build();
        return contactRepository.save(contact);
    }

    public EmergencyContact updateContact(String contactId, AddContactRequest request) {
        Optional<EmergencyContact> optContact = contactRepository.findById(contactId);
        if (optContact.isPresent()) {
            EmergencyContact contact = optContact.get();
            contact.setName(request.getName());
            contact.setPhone(request.getPhone());
            contact.setRelationship(request.getRelationship());
            contact.setEmail(request.getEmail());
            contact.setPrimary(request.isPrimary());
            return contactRepository.save(contact);
        }
        throw new RuntimeException("Contact not found");
    }

    public void deleteContact(String contactId) {
        contactRepository.deleteById(contactId);
    }
}
