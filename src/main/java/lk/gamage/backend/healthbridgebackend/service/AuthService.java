package lk.gamage.backend.healthbridgebackend.service;

import lk.gamage.backend.healthbridgebackend.dto.*;
import lk.gamage.backend.healthbridgebackend.model.AuthProvider;
import lk.gamage.backend.healthbridgebackend.model.OtpToken;
import lk.gamage.backend.healthbridgebackend.model.Role;
import lk.gamage.backend.healthbridgebackend.model.User;
import lk.gamage.backend.healthbridgebackend.repository.OtpTokenRepository;
import lk.gamage.backend.healthbridgebackend.repository.UserRepository;
import lk.gamage.backend.healthbridgebackend.security.CustomUserDetails;
import lk.gamage.backend.healthbridgebackend.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OtpTokenRepository otpTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private EmailService emailService;

    private final SecureRandom secureRandom = new SecureRandom();

    public AuthResponse register(RegisterRequest request) {
        if (request.getConfirmPassword() != null && !request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        if (userRepository.existsByEmail(request.getEmail().toLowerCase().trim())) {
            throw new IllegalArgumentException("An account with this email already exists");
        }

        User user = new User();
        user.setFullName(request.getFullName().trim());
        user.setEmail(request.getEmail().toLowerCase().trim());
        user.setPhoneNumber(request.getPhoneNumber().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.PATIENT);
        user.setProvider(AuthProvider.LOCAL);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        CustomUserDetails userDetails = new CustomUserDetails(savedUser);
        String token = jwtService.generateToken(userDetails, savedUser.getRole(), savedUser.getFullName(), savedUser.getId());

        return new AuthResponse(
                token,
                savedUser.getId(),
                savedUser.getFullName(),
                savedUser.getEmail(),
                savedUser.getRole(),
                "Registration successful"
        );
    }

    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().toLowerCase().trim();
        Optional<User> userOptional = userRepository.findByEmail(normalizedEmail);

        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        User user = userOptional.get();

        if (user.getProvider() == AuthProvider.GOOGLE && (user.getPassword() == null || user.getPassword().isEmpty())) {
            throw new IllegalArgumentException("This account is registered via Google. Please log in with Google.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String token = jwtService.generateToken(userDetails, user.getRole(), user.getFullName(), user.getId());

        return new AuthResponse(
                token,
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                "Login successful"
        );
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        String normalizedEmail = request.getEmail().toLowerCase().trim();
        Optional<User> userOptional = userRepository.findByEmail(normalizedEmail);

        if (userOptional.isEmpty()) {
            // For security, do not reveal whether the user exists, or throw a clean user-friendly exception
            throw new IllegalArgumentException("No account found with this email address");
        }

        // Invalidate previous unused OTP tokens for this email
        List<OtpToken> oldTokens = otpTokenRepository.findAllByEmailAndUsedFalse(normalizedEmail);
        for (OtpToken token : oldTokens) {
            token.setUsed(true);
        }
        otpTokenRepository.saveAll(oldTokens);

        // Generate 6-digit OTP
        int otpCode = 100000 + secureRandom.nextInt(900000);
        String otpString = String.valueOf(otpCode);

        OtpToken otpToken = new OtpToken(
                normalizedEmail,
                otpString,
                LocalDateTime.now().plusMinutes(10)
        );
        otpTokenRepository.save(otpToken);

        // Dispatch Email
        emailService.sendOtpEmail(normalizedEmail, otpString);
    }

    public boolean verifyOtp(OtpVerifyRequest request) {
        String normalizedEmail = request.getEmail().toLowerCase().trim();
        Optional<OtpToken> tokenOpt = otpTokenRepository.findByEmailAndOtpAndUsedFalse(
                normalizedEmail,
                request.getOtp().trim()
        );

        if (tokenOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid verification code");
        }

        OtpToken token = tokenOpt.get();
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Verification code has expired. Please request a new one.");
        }

        return true;
    }

    public AuthResponse resetPassword(ResetPasswordRequest request) {
        String normalizedEmail = request.getEmail().toLowerCase().trim();

        if (request.getConfirmPassword() != null && !request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        // Verify OTP
        Optional<OtpToken> tokenOpt = otpTokenRepository.findByEmailAndOtpAndUsedFalse(
                normalizedEmail,
                request.getOtp().trim()
        );

        if (tokenOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid verification code");
        }

        OtpToken token = tokenOpt.get();
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Verification code has expired. Please request a new one.");
        }

        // Mark OTP as used
        token.setUsed(true);
        otpTokenRepository.save(token);

        // Update User password
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String jwtToken = jwtService.generateToken(userDetails, user.getRole(), user.getFullName(), user.getId());

        return new AuthResponse(
                jwtToken,
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                "Password has been reset successfully"
        );
    }

    public AuthResponse googleAuth(GoogleAuthRequest request) {
        String email = request.getEmail() != null ? request.getEmail().toLowerCase().trim() : "google_user@healthbridge.lk";
        String name = request.getName() != null ? request.getName().trim() : "Google User";

        Optional<User> userOpt = userRepository.findByEmail(email);
        User user;

        if (userOpt.isPresent()) {
            user = userOpt.get();
        } else {
            user = new User();
            user.setEmail(email);
            user.setFullName(name);
            user.setPhoneNumber("");
            user.setRole(Role.PATIENT); // Every Google sign-in is defaulted to PATIENT
            user.setProvider(AuthProvider.GOOGLE);
            user.setGoogleId(request.getToken());
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            user = userRepository.save(user);
        }

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String jwtToken = jwtService.generateToken(userDetails, user.getRole(), user.getFullName(), user.getId());

        return new AuthResponse(
                jwtToken,
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                "Google authentication successful"
        );
    }
}
