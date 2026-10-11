package com.kidsg.backend.security.services;

import com.kidsg.backend.models.OtpVerification;
import com.kidsg.backend.repository.OtpVerificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class OtpService {

    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);
    private static final int OTP_VALIDITY_MINUTES = 10;

    @Value("${supabase.url:https://gokdkyczxyupylenrtwq.supabase.co}")
    private String supabaseUrl;

    @Value("${supabase.anon.key:}")
    private String supabaseAnonKey;

    @Autowired
    private OtpVerificationRepository otpVerificationRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final SecureRandom random = new SecureRandom();

    @Transactional
    public String generateAndSendOtp(String email, String mobileNumber) {
        // 1. Generate local OTP and store in DB
        String otp = String.format("%06d", random.nextInt(1_000_000));
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(OTP_VALIDITY_MINUTES);

        OtpVerification otpVerification = new OtpVerification(email, mobileNumber, otp, expiryTime);
        otpVerificationRepository.save(otpVerification);

        logger.info("=================================================");
        logger.info("[KidsG OTP SERVICE] OTP for email '{}' is: {}", email, otp);
        logger.info("=================================================");

        // 2. Forward to Supabase Auth API to trigger email delivery
        if (supabaseAnonKey != null && !supabaseAnonKey.isBlank()) {
            try {
                String endpoint = supabaseUrl + "/auth/v1/otp";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("apikey", supabaseAnonKey);
                headers.set("Authorization", "Bearer " + supabaseAnonKey);

                Map<String, Object> body = new HashMap<>();
                body.put("email", email);
                body.put("create_user", true);

                HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);
                ResponseEntity<String> response = restTemplate.postForEntity(endpoint, requestEntity, String.class);
                logger.info("[KidsG OTP SERVICE] Supabase email triggered successfully: {}", response.getStatusCode());
            } catch (HttpClientErrorException.TooManyRequests e) {
                logger.warn("[KidsG OTP SERVICE] Supabase email rate limit exceeded (429). Supabase built-in mailer requires custom SMTP: {}", e.getResponseBodyAsString());
            } catch (Exception e) {
                logger.error("[KidsG OTP SERVICE] Supabase OTP forward error: {}", e.getMessage());
            }
        }

        return otp;
    }

    @Transactional
    public boolean verifyOtp(String email, String inputOtp) {
        // Check local DB verification
        Optional<OtpVerification> recordOpt = otpVerificationRepository.findTopByEmailOrderByExpiryTimeDesc(email);
        if (recordOpt.isPresent()) {
            OtpVerification record = recordOpt.get();
            if (!record.getExpiryTime().isBefore(LocalDateTime.now()) && record.getOtp().equals(inputOtp)) {
                record.setVerified(true);
                otpVerificationRepository.save(record);
                return true;
            }
        }

        // Also check Supabase verification
        if (supabaseAnonKey != null && !supabaseAnonKey.isBlank()) {
            try {
                String endpoint = supabaseUrl + "/auth/v1/verify";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("apikey", supabaseAnonKey);
                headers.set("Authorization", "Bearer " + supabaseAnonKey);

                Map<String, Object> body = new HashMap<>();
                body.put("type", "email");
                body.put("email", email);
                body.put("token", inputOtp);

                HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);
                ResponseEntity<String> response = restTemplate.postForEntity(endpoint, requestEntity, String.class);
                if (response.getStatusCode().is2xxSuccessful()) {
                    OtpVerification verification = recordOpt.orElse(new OtpVerification(email, null, inputOtp, LocalDateTime.now().plusMinutes(10)));
                    verification.setVerified(true);
                    otpVerificationRepository.save(verification);
                    return true;
                }
            } catch (Exception e) {
                logger.debug("[KidsG OTP SERVICE] Supabase verify error: {}", e.getMessage());
            }
        }

        return false;
    }

    public boolean isEmailVerified(String email) {
        Optional<OtpVerification> recordOpt = otpVerificationRepository.findTopByEmailOrderByExpiryTimeDesc(email);
        return recordOpt.isPresent() && recordOpt.get().isVerified() && !recordOpt.get().getExpiryTime().isBefore(LocalDateTime.now());
    }

    @Transactional
    public void clearOtpForEmail(String email) {
        try {
            otpVerificationRepository.deleteByEmail(email);
        } catch (Exception ignored) {
        }
    }
}
