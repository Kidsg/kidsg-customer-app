package com.kidsg.backend.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class VerifyOtpRequest {

    @Size(max = 50)
    private String email;

    @Size(max = 20)
    private String phone;

    @NotBlank(message = "OTP is mandatory")
    @Size(min = 4, max = 8, message = "OTP must be between 4 and 8 characters")
    private String otp;

    public VerifyOtpRequest() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getTargetContact() {
        if (email != null && !email.trim().isEmpty()) {
            return email.trim();
        }
        if (phone != null && !phone.trim().isEmpty()) {
            return phone.trim();
        }
        return null;
    }
}
