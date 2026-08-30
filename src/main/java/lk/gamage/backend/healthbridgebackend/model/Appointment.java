package lk.gamage.backend.healthbridgebackend.model;

import lk.gamage.backend.healthbridgebackend.enums.AppointmentStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "appointments")
public class Appointment {
    @Id
    private String id;
    private String patientId;
    private String doctorId;
    private String doctorName;
    private String doctorSpecialization;
    private String hospital;
    private LocalDate appointmentDate;
    private String appointmentTime;
    private String appointmentType;
    private String reason;
    private AppointmentStatus status;
    private String cancellationReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Appointment() { }

    public Appointment(String id, String patientId, String doctorId, String doctorName,
                       String doctorSpecialization, String hospital, LocalDate appointmentDate,
                       String appointmentTime, String appointmentType, String reason) {
        this.id = id; this.patientId = patientId; this.doctorId = doctorId;
        this.doctorName = doctorName; this.doctorSpecialization = doctorSpecialization;
        this.hospital = hospital; this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime; this.appointmentType = appointmentType;
        this.reason = reason; this.status = AppointmentStatus.UPCOMING;
        this.createdAt = LocalDateTime.now(); this.updatedAt = this.createdAt;
    }

    public String getId() { return id; } public void setId(String v) { id = v; }
    public String getPatientId() { return patientId; } public void setPatientId(String v) { patientId = v; }
    public String getDoctorId() { return doctorId; } public void setDoctorId(String v) { doctorId = v; }
    public String getDoctorName() { return doctorName; } public void setDoctorName(String v) { doctorName = v; }
    public String getDoctorSpecialization() { return doctorSpecialization; } public void setDoctorSpecialization(String v) { doctorSpecialization = v; }
    public String getHospital() { return hospital; } public void setHospital(String v) { hospital = v; }
    public LocalDate getAppointmentDate() { return appointmentDate; } public void setAppointmentDate(LocalDate v) { appointmentDate = v; }
    public String getAppointmentTime() { return appointmentTime; } public void setAppointmentTime(String v) { appointmentTime = v; }
    public String getAppointmentType() { return appointmentType; } public void setAppointmentType(String v) { appointmentType = v; }
    public String getReason() { return reason; } public void setReason(String v) { reason = v; }
    public AppointmentStatus getStatus() { return status; } public void setStatus(AppointmentStatus v) { status = v; }
    public String getCancellationReason() { return cancellationReason; } public void setCancellationReason(String v) { cancellationReason = v; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime v) { createdAt = v; }
    public LocalDateTime getUpdatedAt() { return updatedAt; } public void setUpdatedAt(LocalDateTime v) { updatedAt = v; }
}
