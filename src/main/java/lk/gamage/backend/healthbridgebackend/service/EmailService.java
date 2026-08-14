package lk.gamage.backend.healthbridgebackend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:healthbridge012@gmail.com}")
    private String fromEmail;

    public void sendOtpEmail(String toEmail, String otp) {
        log.info("==================================================");
        log.info("[Health Bridge OTP Verification]");
        log.info("Sending OTP code: {} to {}", otp, toEmail);
        log.info("==================================================");

        if (mailSender == null) {
            log.warn("JavaMailSender is not configured. OTP printed to console.");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "Health Bridge Security");
            helper.setTo(toEmail);
            helper.setSubject("Your Health Bridge Verification Code");

            String htmlContent = "<div style=\"font-family: 'Segoe UI', Arial, sans-serif; max-width: 540px; margin: auto; padding: 32px; border: 1px solid #e5e7eb; border-radius: 16px; background: #ffffff;\">"
                    + "<div style=\"display: flex; align-items: center; gap: 8px; margin-bottom: 24px;\">"
                    + "<h2 style=\"color: #1d4ed8; margin: 0; font-size: 24px;\">🏥 Health Bridge</h2>"
                    + "</div>"
                    + "<h3 style=\"color: #111827; font-size: 20px; margin-top: 0;\">Verify Your Identity</h3>"
                    + "<p style=\"color: #4b5563; font-size: 15px; line-height: 1.6;\">You have requested a verification code to access or reset your Health Bridge account. Use the 6-digit code below to proceed:</p>"
                    + "<div style=\"background: #f0fdf4; border: 1px solid #bbf7d0; border-radius: 12px; padding: 20px; text-align: center; margin: 24px 0;\">"
                    + "<span style=\"font-size: 36px; font-weight: 700; letter-spacing: 8px; color: #15803d; font-family: monospace;\">" + otp + "</span>"
                    + "</div>"
                    + "<p style=\"color: #6b7280; font-size: 13px;\">⏱️ This code will expire in <strong>10 minutes</strong>. If you did not make this request, you can safely ignore this email.</p>"
                    + "<hr style=\"border: none; border-top: 1px solid #f3f4f6; margin: 24px 0;\" />"
                    + "<p style=\"color: #9ca3af; font-size: 12px; text-align: center; margin: 0;\">© 2026 Health Bridge. Secure Healthcare Management System.</p>"
                    + "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("OTP email successfully dispatched to {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send OTP email via SMTP to {}: {}. OTP is: {}", toEmail, e.getMessage(), otp);
        }
    }
}
