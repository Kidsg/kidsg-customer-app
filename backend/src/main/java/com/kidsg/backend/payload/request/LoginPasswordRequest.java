package com.kidsg.backend.payload.request;

import jakarta.validation.constraints.NotBlank;

public class LoginPasswordRequest {
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    public LoginPasswordRequest() {
    }

    public LoginPasswordRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
