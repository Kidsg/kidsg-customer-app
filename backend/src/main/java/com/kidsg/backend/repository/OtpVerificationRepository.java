package com.kidsg.backend.repository;

import com.kidsg.backend.models.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    Optional<OtpVerification> findTopByEmailOrderByExpiryTimeDesc(String email);

    Optional<OtpVerification> findTopByMobileNumberOrderByExpiryTimeDesc(String mobileNumber);

    void deleteByEmail(String email);

    void deleteByMobileNumber(String mobileNumber);
}
