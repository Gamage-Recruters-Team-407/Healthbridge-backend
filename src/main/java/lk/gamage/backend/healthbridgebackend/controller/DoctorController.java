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
public class DoctorController {
    private final UserRepository users;
    private final lk.gamage.backend.healthbridgebackend.service.DoctorMetricsService metrics;

    public DoctorController(UserRepository users, lk.gamage.backend.healthbridgebackend.service.DoctorMetricsService metrics) {
        this.users = users;
        this.metrics = metrics;
    }

    public record Profile(String id, String fullName, String email, String phoneNumber,
            String profileImage, String gender, String dateOfBirth, String address,
            String specialization, List<String> qualifications, int experience,
            double consultationFee, double rating, boolean availableToday, String bio) {}

    /**
     * Return strictly the doctor user accounts from User Management (users collection).
     */
    @GetMapping
    public List<Profile> getAllDoctors() {
        return users.findAll().stream()
                .filter(u -> u.getRole() != null && u.getRole().toUpperCase().contains("DOCTOR"))
                .map(this::profile)
                .toList();
    }

    @GetMapping("/me/earnings")
    @PreAuthorize("hasRole('DOCTOR')")
    public lk.gamage.backend.healthbridgebackend.service.DoctorMetricsService.Earnings earnings(@AuthenticationPrincipal CustomUserDetails principal) {
        return metrics.get(principal.getId());
    }

    public record ProfileUpdate(@NotBlank String fullName, @NotBlank @Email String email,
            @NotBlank @Pattern(regexp = "(?:0[1-9]\\d{8}|\\+94[1-9]\\d{8})") String phoneNumber,
            @Size(max = 3000000) String profileImage, String gender, String dateOfBirth, String address,
            @NotBlank String specialization, @NotNull List<String> qualifications,
            @Min(0) int experience, @PositiveOrZero double consultationFee, String bio) {}

    @GetMapping("/me")
    @PreAuthorize("hasRole('DOCTOR')")
    public Profile get(@AuthenticationPrincipal CustomUserDetails principal) {
        return profile(requireUser(principal));
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('DOCTOR')")
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
        String name = value(user.getFullName()).trim();
        if (name.isEmpty() && user.getFirstName() != null) {
            name = (user.getFirstName() + " " + value(user.getLastName())).trim();
        }
        if (!name.isEmpty() && !name.toLowerCase().startsWith("dr.") && !name.toLowerCase().startsWith("dr ")) {
            name = "Dr. " + name;
        }

        String spec = value(user.getSpecialization()).trim();
        if (spec.isEmpty() || spec.equalsIgnoreCase("Genral") || spec.equalsIgnoreCase("General")) {
            spec = "General Medicine";
        }

        String image = value(user.getPicture()).trim();
        if (image.isEmpty()) {
            int seed = Math.abs((user.getEmail() != null ? user.getEmail() : user.getId()).hashCode()) % 70 + 1;
            image = "https://i.pravatar.cc/320?img=" + seed;
        }

        String address = value(user.getAddress()).trim();
        if (address.isEmpty()) {
            address = "Colombo, Sri Lanka";
        }

        List<String> qualifications = user.getQualifications();
        if (qualifications == null || qualifications.isEmpty()) {
            qualifications = List.of("MBBS", "MD");
        }

        int exp = user.getExperience() == null || user.getExperience() <= 0 ? 8 : user.getExperience();
        double fee = user.getConsultationFee() == null || user.getConsultationFee() <= 0 ? 3500.0 : user.getConsultationFee();
        String bio = value(user.getBio()).trim();
        if (bio.isEmpty()) {
            bio = "Consultant Specialist at HealthBridge Hospital.";
        }

        return new Profile(
                user.getId(),
                name,
                user.getEmail(),
                value(user.getPhoneNumber()),
                image,
                user.getGender() == null || user.getGender().isBlank() ? "Other" : user.getGender(),
                value(user.getDateOfBirth()),
                address,
                spec,
                qualifications,
                exp,
                fee,
                4.9,
                true,
                bio
        );
    }

    private String value(String value) { return value == null ? "" : value; }
}
