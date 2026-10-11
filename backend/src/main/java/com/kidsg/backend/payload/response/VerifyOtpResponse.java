package com.kidsg.backend.payload.response;

public class VerifyOtpResponse {
    private boolean verified;
    private String token;
    private UserDto user;
    private ProfileDto profile;
    private boolean isNewUser;

    public VerifyOtpResponse() {
        this.verified = true;
    }

    public VerifyOtpResponse(boolean verified, String token, UserDto user, ProfileDto profile, boolean isNewUser) {
        this.verified = verified;
        this.token = token;
        this.user = user;
        this.profile = profile;
        this.isNewUser = isNewUser;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserDto getUser() {
        return user;
    }

    public void setUser(UserDto user) {
        this.user = user;
    }

    public ProfileDto getProfile() {
        return profile;
    }

    public void setProfile(ProfileDto profile) {
        this.profile = profile;
    }

    public boolean isNewUser() {
        return isNewUser;
    }

    public void setNewUser(boolean newUser) {
        isNewUser = newUser;
    }
}
