package com.kidsg.backend.payload.response;

public class CheckEmailResponse {
    private boolean exists;
    private String firstName;

    public CheckEmailResponse() {
    }

    public CheckEmailResponse(boolean exists, String firstName) {
        this.exists = exists;
        this.firstName = firstName;
    }

    public boolean isExists() {
        return exists;
    }

    public void setExists(boolean exists) {
        this.exists = exists;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
}
