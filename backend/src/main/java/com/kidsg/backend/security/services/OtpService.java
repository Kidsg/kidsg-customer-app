package com.kidsg.backend.security.services;

import com.kidsg.backend.models.OtpVerification;
import com.kidsg.backend.repository.OtpVerificationRepository;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class OtpService {

    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);
    private static final int OTP_VALIDITY_MINUTES = 10;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:veenithkumars31@gmail.com}")
    private String mailFrom;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    @Autowired
    private OtpVerificationRepository otpVerificationRepository;

    private final SecureRandom random = new SecureRandom();

    @Transactional
    public String generateAndSendOtp(String email, String mobileNumber) {
        // Normalize
        String effectiveEmail = (email != null && !email.isBlank()) ? email.trim() : null;
        String effectiveMobile = (mobileNumber != null && !mobileNumber.isBlank()) ? mobileNumber.trim() : null;

        // If email is absent but mobile is present, create internal identifier
        if (effectiveEmail == null && effectiveMobile != null) {
            effectiveEmail = effectiveMobile + "@kidsg.in";
        }

        // 1. Generate 6-digit OTP and store in DB
        String otp = String.format("%06d", random.nextInt(1_000_000));
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(OTP_VALIDITY_MINUTES);

        OtpVerification otpVerification = new OtpVerification(effectiveEmail, effectiveMobile, otp, expiryTime);
        otpVerificationRepository.save(otpVerification);

        logger.info("=================================================================");
        logger.info("[KidsG OTP SERVICE] OTP generated for '{}': {}", effectiveEmail, otp);
        logger.info("=================================================================");

        // 2. Dispatch real email via Gmail SMTP using Google App Password
        if (effectiveEmail != null && effectiveEmail.contains("@") && !effectiveEmail.endsWith("@kidsg.in")) {
            sendOtpEmailViaGmail(effectiveEmail, otp);
        }

        return otp;
    }

    /**
     * Sends a beautifully formatted HTML OTP email via Gmail SMTP using Google App Password.
     */
    public boolean sendOtpEmailViaGmail(String recipientEmail, String otp) {
        if (mailSender == null || mailPassword == null || mailPassword.isBlank()) {
            logger.warn("[KidsG MAIL] Gmail App Password not configured in GMAIL_APP_PASSWORD. Logged OTP to console: {}", otp);
            return false;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(mailFrom, "KidsG — Student Superstore");
            helper.setTo(recipientEmail);
            helper.setSubject("KidsG Verification Code: " + otp);

            String htmlBody = """
                <!DOCTYPE html>
                <html>
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>KidsG OTP Verification</title>
                </head>
                <body style="margin: 0; padding: 0; background-color: #F8FAFC; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; color: #1E293B;">
                  <table align="center" border="0" cellpadding="0" cellspacing="0" width="100%%" style="max-width: 560px; margin: 30px auto; background-color: #FFFFFF; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05); border: 1px solid #E2E8F0;">
                    <!-- Header -->
                    <tr>
                      <td style="padding: 32px 32px 24px; text-align: center; background: linear-gradient(135deg, #FF6F00 0%%, #FF9100 100%%);">
                        <h1 style="margin: 0; color: #FFFFFF; font-size: 28px; font-weight: 800; letter-spacing: -0.5px;">KidsG</h1>
                        <p style="margin: 6px 0 0; color: rgba(255, 255, 255, 0.9); font-size: 14px; font-weight: 500;">India's Favourite School & Student Superstore</p>
                      </td>
                    </tr>
                    
                    <!-- Content -->
                    <tr>
                      <td style="padding: 36px 32px;">
                        <h2 style="margin: 0 0 12px; color: #0F172A; font-size: 20px; font-weight: 700; text-align: center;">One-Time Verification Code</h2>
                        <p style="margin: 0 0 24px; color: #475569; font-size: 15px; line-height: 1.6; text-align: center;">
                          Use the code below to sign in or verify your KidsG account. This code is confidential and valid for <strong>10 minutes</strong>.
                        </p>
                        
                        <!-- OTP Box -->
                        <div style="background-color: #FFF7ED; border: 2px dashed #FDBA74; border-radius: 12px; padding: 20px; text-align: center; margin: 24px 0;">
                          <span style="display: block; font-size: 38px; font-weight: 800; letter-spacing: 10px; color: #EA580C; font-family: 'SF Mono', Monaco, Consolas, monospace;">%s</span>
                        </div>
                        
                        <!-- Expiration & Security -->
                        <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="margin-top: 24px;">
                          <tr>
                            <td style="padding: 12px; background-color: #F1F5F9; border-radius: 8px; font-size: 13px; color: #64748B; line-height: 1.5;">
                              🔒 <strong>Security Tip:</strong> Never share this code with anyone, including KidsG representatives. If you did not request this OTP, you can safely ignore this email.
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                    
                    <!-- Footer -->
                    <tr>
                      <td style="padding: 20px 32px; background-color: #F8FAFC; border-top: 1px solid #E2E8F0; text-align: center; font-size: 12px; color: #94A3B8;">
                        © 2026 KidsG Retail Private Limited. All rights reserved.<br>
                        Support: <a href="mailto:support@kidsg.in" style="color: #FF6F00; text-decoration: none;">support@kidsg.in</a>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(otp);

            helper.setText(htmlBody, true);
            mailSender.send(message);
            logger.info("[KidsG MAIL] OTP email successfully sent to {}", recipientEmail);
            return true;
        } catch (Exception e) {
            logger.error("[KidsG MAIL] Failed to send email to {}: {}", recipientEmail, e.getMessage());
            return false;
        }
    }

    @Transactional
    public boolean verifyOtp(String contact, String inputOtp) {
        if (contact == null || inputOtp == null) {
            return false;
        }

        String trimmed = contact.trim();

        // 1. Search by email
        Optional<OtpVerification> recordOpt = otpVerificationRepository.findTopByEmailOrderByExpiryTimeDesc(trimmed);

        // 2. Search by mobile number if not found by email
        if (recordOpt.isEmpty()) {
            recordOpt = otpVerificationRepository.findTopByMobileNumberOrderByExpiryTimeDesc(trimmed);
        }

        // 3. Search with fallback phone identifier
        if (recordOpt.isEmpty() && !trimmed.contains("@")) {
            recordOpt = otpVerificationRepository.findTopByEmailOrderByExpiryTimeDesc(trimmed + "@kidsg.in");
        }

        if (recordOpt.isPresent()) {
            OtpVerification record = recordOpt.get();
            if (!record.getExpiryTime().isBefore(LocalDateTime.now()) && record.getOtp().equals(inputOtp.trim())) {
                record.setVerified(true);
                otpVerificationRepository.save(record);
                return true;
            }
        }

        return false;
    }

    public boolean isEmailVerified(String email) {
        if (email == null) return false;
        Optional<OtpVerification> recordOpt = otpVerificationRepository.findTopByEmailOrderByExpiryTimeDesc(email.trim());
        return recordOpt.isPresent() && recordOpt.get().isVerified() && !recordOpt.get().getExpiryTime().isBefore(LocalDateTime.now());
    }

    @Transactional
    public void clearOtpForEmail(String email) {
        try {
            if (email != null) {
                otpVerificationRepository.deleteByEmail(email.trim());
            }
        } catch (Exception ignored) {
        }
    }
}
