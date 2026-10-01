package lk.gamage.backend.healthbridgebackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.time.LocalTime;

public record DoctorSessionRequest(
        String doctorId,
        String hospitalId,
        String hospitalName,
        String specializationId,
        @NotBlank String specializationName,
        @NotNull LocalDate sessionDate,
        @NotNull LocalTime startTime,
        LocalTime endTime,
        @Positive int maxAppointments,
        String notes) { }
