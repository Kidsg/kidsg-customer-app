package com.kidsg.backend.payload.request;

import jakarta.validation.constraints.Size;

public class SendOtpRequest {

    @Size(max = 50)
    private String email;

    @Size(max = 20)
    private String phone;

    @Size(max = 20)
    private String mobileNumber;

    public SendOtpRequest() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone != null ? phone : mobileNumber;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMobileNumber() {
        return mobileNumber != null ? mobileNumber : phone;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    /**
     * Resolves the primary contact destination (either email or phone).
     */
    public String getTargetContact() {
        if (email != null && !email.trim().isEmpty()) {
            return email.trim();
        }
        if (phone != null && !phone.trim().isEmpty()) {
            return phone.trim();
        }
        if (mobileNumber != null && !mobileNumber.trim().isEmpty()) {
            return mobileNumber.trim();
        }
        return null;
    }
}
