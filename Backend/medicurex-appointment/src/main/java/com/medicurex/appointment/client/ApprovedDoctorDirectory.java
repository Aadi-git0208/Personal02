package com.medicurex.appointment.client;

import java.util.List;

public class ApprovedDoctorDirectory {

    private Long id;
    private Long authUserId;
    private String fullName;
    private String specialization;
    private List<String> availabilitySlots;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAuthUserId() {
        return authUserId;
    }

    public void setAuthUserId(Long authUserId) {
        this.authUserId = authUserId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public List<String> getAvailabilitySlots() {
        return availabilitySlots;
    }

    public void setAvailabilitySlots(List<String> availabilitySlots) {
        this.availabilitySlots = availabilitySlots;
    }
}
