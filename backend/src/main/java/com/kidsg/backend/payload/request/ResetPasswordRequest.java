package com.kidsg.backend.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ResetPasswordRequest {

    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email must be a valid email address")
    @Size(max = 50)
    private String email;

    @NotBlank(message = "OTP is mandatory")
    @Size(min = 4, max = 6, message = "OTP must be between 4 and 6 characters")
    private String otp;

    @NotBlank(message = "New password is mandatory")
    @Size(min = 6, max = 40, message = "Password must be at least 6 characters")
    private String newPassword;

    @NotBlank(message = "Confirm password is mandatory")
    @Size(min = 6, max = 40, message = "Confirm password must be at least 6 characters")
    private String confirmPassword;

    public ResetPasswordRequest() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
