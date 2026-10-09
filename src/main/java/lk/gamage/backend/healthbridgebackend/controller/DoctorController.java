package lk.gamage.backend.healthbridgebackend.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;
import lk.gamage.backend.healthbridgebackend.model.User;
import lk.gamage.backend.healthbridgebackend.repository.UserRepository;
import lk.gamage.backend.healthbridgebackend.security.CustomUserDetails;
import lk.gamage.backend.healthbridgebackend.exception.BadRequestException;
import lk.gamage.backend.healthbridgebackend.exception.ResourceNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/doctors")
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorController {
    private final UserRepository users;
    private final lk.gamage.backend.healthbridgebackend.service.DoctorMetricsService metrics;
    public DoctorController(UserRepository users, lk.gamage.backend.healthbridgebackend.service.DoctorMetricsService metrics) { this.users = users; this.metrics = metrics; }

    @GetMapping("/me/earnings")
    public lk.gamage.backend.healthbridgebackend.service.DoctorMetricsService.Earnings earnings(@AuthenticationPrincipal CustomUserDetails principal) {
        return metrics.get(principal.getId());
    }

    public record ProfileUpdate(@NotBlank String fullName, @NotBlank @Email String email,
            @NotBlank @Pattern(regexp = "(?:0[1-9]\\d{8}|\\+94[1-9]\\d{8})") String phoneNumber,
            @Size(max = 3000000) String profileImage, String gender, String dateOfBirth, String address,
            @NotBlank String specialization, @NotNull List<String> qualifications,
            @Min(0) int experience, @PositiveOrZero double consultationFee, String bio) {}

    public record Profile(String id, String fullName, String email, String phoneNumber,
            String profileImage, String gender, String dateOfBirth, String address,
            String specialization, List<String> qualifications, int experience,
            double consultationFee, double rating, boolean availableToday, String bio) {}

    @GetMapping("/me")
    public Profile get(@AuthenticationPrincipal CustomUserDetails principal) {
        return profile(requireUser(principal));
    }

    @PutMapping("/me")
    public Profile update(@AuthenticationPrincipal CustomUserDetails principal, @Valid @RequestBody ProfileUpdate input) {
        User user = requireUser(principal);
        // Changing the login identity requires the separate account flow.
        if (!user.getEmail().equalsIgnoreCase(input.email().trim())) {
            throw new BadRequestException("Change your email through account settings before updating your profile.");
        }
        user.setFullName(input.fullName().trim());
        user.setPhoneNumber(input.phoneNumber());
        user.setPhone(input.phoneNumber());
        user.setPicture(input.profileImage());
        user.setGender(input.gender());
        user.setDateOfBirth(input.dateOfBirth());
        user.setAddress(input.address());
        user.setSpecialization(input.specialization().trim());
        user.setQualifications(input.qualifications());
        user.setExperience(input.experience());
        user.setConsultationFee(input.consultationFee());
        user.setBio(input.bio());
        user.setUpdatedAt(LocalDateTime.now());
        return profile(users.save(user));
    }

    private User requireUser(CustomUserDetails principal) {
        return users.findById(principal.getId()).orElseThrow(() -> new ResourceNotFoundException("Doctor account not found."));
    }

    private Profile profile(User user) {
        return new Profile(user.getId(), value(user.getFullName()), user.getEmail(), value(user.getPhoneNumber()),
                value(user.getPicture()), user.getGender() == null ? "Other" : user.getGender(),
                value(user.getDateOfBirth()), value(user.getAddress()), value(user.getSpecialization()),
                user.getQualifications() == null ? List.of() : user.getQualifications(),
                user.getExperience() == null ? 0 : user.getExperience(),
                user.getConsultationFee() == null ? 0 : user.getConsultationFee(), 0, false, user.getBio());
    }
    private String value(String value) { return value == null ? "" : value; }
}
