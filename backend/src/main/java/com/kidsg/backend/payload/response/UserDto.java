package com.kidsg.backend.payload.response;

public class UserDto {
    private String id;
    private String phone;
    private String email;
    private String role;
    private String firstName;
    private String lastName;

    public UserDto() {
    }

    public UserDto(String id, String phone, String email, String role, String firstName, String lastName) {
        this.id = id;
        this.phone = phone;
        this.email = email;
        this.role = role != null ? role : "CUSTOMER";
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
}
