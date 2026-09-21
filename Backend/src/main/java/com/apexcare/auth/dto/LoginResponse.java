package com.apexcare.auth.dto;

public class LoginResponse {

    private String token;
    private String type;
    private UserResponse user;

    public LoginResponse() {
    }

    public LoginResponse(String token, String type, UserResponse user) {
        this.token = token;
        this.type = type;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public String getType() {
        return type;
    }

    public UserResponse getUser() {
        return user;
    }
}
