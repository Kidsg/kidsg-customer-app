package com.kidsg.backend.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class CheckEmailRequest {
    @NotBlank
    @Email
    private String email;

    public CheckEmailRequest() {
    }

    public CheckEmailRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
