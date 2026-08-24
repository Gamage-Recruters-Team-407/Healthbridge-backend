package lk.gamage.backend.healthbridgebackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lk.gamage.backend.healthbridgebackend.model.TicketCategory;
import lk.gamage.backend.healthbridgebackend.model.TicketPriority;

public class CreateSupportTicketRequest {

    @NotNull
    private String patientId;

    @NotBlank
    private String subject;

    @NotNull
    private TicketCategory category;

    @NotBlank
    private String description;

    @NotNull
    private TicketPriority priority;

    @NotBlank
    private String mobileNumber;

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public TicketCategory getCategory() {
        return category;
    }

    public void setCategory(TicketCategory category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public void setPriority(TicketPriority priority) {
        this.priority = priority;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }
}