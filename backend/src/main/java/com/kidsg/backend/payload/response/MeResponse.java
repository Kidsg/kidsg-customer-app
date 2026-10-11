package com.kidsg.backend.payload.response;

import java.util.ArrayList;
import java.util.List;

public class MeResponse {
    private UserDto user;
    private ProfileDto profile;
    private List<Object> addresses;

    public MeResponse() {
        this.addresses = new ArrayList<>();
    }

    public MeResponse(UserDto user, ProfileDto profile) {
        this.user = user;
        this.profile = profile;
        this.addresses = new ArrayList<>();
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

    public List<Object> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Object> addresses) {
        this.addresses = addresses;
    }
}
