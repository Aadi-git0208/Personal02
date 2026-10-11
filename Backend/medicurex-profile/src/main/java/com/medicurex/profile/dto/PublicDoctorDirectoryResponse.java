package com.medicurex.profile.dto;

import com.medicurex.profile.model.DoctorProfile;

import java.math.BigDecimal;
import java.util.List;

public class PublicDoctorDirectoryResponse {

    private Long id;
    private Long authUserId;
    private String fullName;
    private String specialization;
    private String qualifications;
    private Integer experienceYears;
    private BigDecimal consultationFee;
    private String clinicAddress;
    private String biography;
    private List<String> availabilitySlots;

    public static PublicDoctorDirectoryResponse from(DoctorProfile profile) {
        PublicDoctorDirectoryResponse response = new PublicDoctorDirectoryResponse();
        response.id = profile.getId();
        response.authUserId = profile.getAuthUserId();
        response.fullName = profile.getFullName();
        response.specialization = profile.getSpecialization();
        response.qualifications = profile.getQualifications();
        response.experienceYears = profile.getExperienceYears();
        response.consultationFee = profile.getConsultationFee();
        response.clinicAddress = profile.getClinicAddress();
        response.biography = profile.getBiography();
        response.availabilitySlots = List.copyOf(
                profile.getAvailabilitySlots() == null ? List.of() : profile.getAvailabilitySlots()
        );
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

    public String getBiography() {
        return biography;
    }

    public List<String> getAvailabilitySlots() {
        return availabilitySlots;
    }
}
