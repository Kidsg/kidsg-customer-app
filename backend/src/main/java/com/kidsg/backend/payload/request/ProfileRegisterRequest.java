package com.kidsg.backend.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProfileRegisterRequest {

    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email must be a valid email address")
    @Size(max = 50)
    private String email;

    @Size(max = 20)
    private String mobileNumber;

    @NotBlank(message = "Kid's name is mandatory")
    @Size(max = 100)
    private String kidName;

    private Integer age;

    @Size(max = 50)
    private String grade;

    @Size(max = 100)
    private String parentName;

    @NotBlank(message = "Password is mandatory")
    @Size(min = 6, max = 40, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Confirm password is mandatory")
    @Size(min = 6, max = 40, message = "Confirm password must be at least 6 characters")
    private String confirmPassword;

    public ProfileRegisterRequest() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getKidName() {
        return kidName;
    }

    public void setKidName(String kidName) {
        this.kidName = kidName;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
