package com.medicurex.profile.dto;

import com.medicurex.profile.model.PatientProfile;

import java.time.Instant;
import java.time.LocalDate;

public class PatientProfileResponse {

    private Long id;
    private Long authUserId;
    private String fullName;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String gender;
    private String address;
    private String bloodGroup;
    private Instant createdAt;
    private Instant updatedAt;

    public static PatientProfileResponse from(PatientProfile profile) {
        PatientProfileResponse response = new PatientProfileResponse();
        response.id = profile.getId();
        response.authUserId = profile.getAuthUserId();
        response.fullName = profile.getFullName();
        response.phoneNumber = profile.getPhoneNumber();
        response.dateOfBirth = profile.getDateOfBirth();
        response.gender = profile.getGender();
        response.address = profile.getAddress();
        response.bloodGroup = profile.getBloodGroup();
        response.createdAt = profile.getCreatedAt();
        response.updatedAt = profile.getUpdatedAt();
        return response;
    }

    public Long getId() {
        return id;
    }

    public Long getAuthUserId() {
        return authUserId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public String getAddress() {
        return address;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
