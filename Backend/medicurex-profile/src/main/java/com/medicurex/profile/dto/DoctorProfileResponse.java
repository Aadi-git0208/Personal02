package com.medicurex.profile.dto;

import com.medicurex.profile.model.ApprovalStatus;
import com.medicurex.profile.model.DoctorProfile;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class DoctorProfileResponse {

    private Long id;
    private Long authUserId;
    private String fullName;
    private String specialization;
    private String qualifications;
    private Integer experienceYears;
    private BigDecimal consultationFee;
    private String clinicAddress;
    private String phoneNumber;
    private String biography;
    private List<String> availabilitySlots;
    private boolean profileCompleted;
    private ApprovalStatus approvalStatus;
    private Instant createdAt;
    private Instant updatedAt;

    public static DoctorProfileResponse from(DoctorProfile profile) {
        DoctorProfileResponse response = new DoctorProfileResponse();
        response.id = profile.getId();
        response.authUserId = profile.getAuthUserId();
        response.fullName = profile.getFullName();
        response.specialization = profile.getSpecialization();
        response.qualifications = profile.getQualifications();
        response.experienceYears = profile.getExperienceYears();
        response.consultationFee = profile.getConsultationFee();
        response.clinicAddress = profile.getClinicAddress();
        response.phoneNumber = profile.getPhoneNumber();
        response.biography = profile.getBiography();
        response.availabilitySlots = List.copyOf(
                profile.getAvailabilitySlots() == null ? List.of() : profile.getAvailabilitySlots()
        );
        response.profileCompleted = profile.isProfileCompleted();
        response.approvalStatus = profile.getApprovalStatus();
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

    public String getSpecialization() {
        return specialization;
    }

    public String getQualifications() {
        return qualifications;
    }

    public Integer getExperienceYears() {
        return experienceYears;
    }

    public BigDecimal getConsultationFee() {
        return consultationFee;
    }

    public String getClinicAddress() {
        return clinicAddress;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getBiography() {
        return biography;
    }

    public List<String> getAvailabilitySlots() {
        return availabilitySlots;
    }

    public boolean isProfileCompleted() {
        return profileCompleted;
    }

    public ApprovalStatus getApprovalStatus() {
        return approvalStatus;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
