package com.kidsg.backend.payload.response;

public class ProfileDto {
    private String id;
    private String authUserId;
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private String role;
    private boolean onboardingCompleted;
    private String selectedClass;
    private String selectedSchool;
    private String avatarUrl;

    public ProfileDto() {
    }

    public ProfileDto(String id, String authUserId, String firstName, String lastName, String phone, String email,
                      String role, boolean onboardingCompleted, String selectedClass, String selectedSchool, String avatarUrl) {
        this.id = id;
        this.authUserId = authUserId;
        this.firstName = firstName != null ? firstName : "";
        this.lastName = lastName != null ? lastName : "";
        this.phone = phone;
        this.email = email;
        this.role = role != null ? role : "CUSTOMER";
        this.onboardingCompleted = onboardingCompleted;
        this.selectedClass = selectedClass;
        this.selectedSchool = selectedSchool;
        this.avatarUrl = avatarUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthUserId() {
        return authUserId;
    }

    public void setAuthUserId(String authUserId) {
        this.authUserId = authUserId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isOnboardingCompleted() {
        return onboardingCompleted;
    }

    public void setOnboardingCompleted(boolean onboardingCompleted) {
        this.onboardingCompleted = onboardingCompleted;
    }

    public String getSelectedClass() {
        return selectedClass;
    }

    public void setSelectedClass(String selectedClass) {
        this.selectedClass = selectedClass;
    }

    public String getSelectedSchool() {
        return selectedSchool;
    }

    public void setSelectedSchool(String selectedSchool) {
        this.selectedSchool = selectedSchool;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
