package lk.gamage.backend.healthbridgebackend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "doctor_leaves")
public class DoctorLeave {

    @Id
    private String id;
    private String doctorId;
    private String doctorEmail;
    private String leaveType;
    private String startDate;
    private String endDate;
    private String reason;
    private String status;
    private String appliedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public DoctorLeave() {
    }

    public DoctorLeave(String id, String doctorId, String doctorEmail, String leaveType, String startDate,
                       String endDate, String reason, String status, String appliedAt,
                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.doctorId = doctorId;
        this.doctorEmail = doctorEmail;
        this.leaveType = leaveType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
        this.status = status;
        this.appliedAt = appliedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getDoctorEmail() {
        return doctorEmail;
    }

    public void setDoctorEmail(String doctorEmail) {
        this.doctorEmail = doctorEmail;
    }

    public String getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(String leaveType) {
        this.leaveType = leaveType;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(String appliedAt) {
        this.appliedAt = appliedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
