package com.medicurex.profile.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DoctorProfileRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @NotBlank(message = "Specialization is required")
    @Size(max = 120, message = "Specialization must be at most 120 characters")
    private String specialization;

    @NotBlank(message = "Qualifications are required")
    @Size(max = 255, message = "Qualifications must be at most 255 characters")
    private String qualifications;

    @NotNull(message = "Experience in years is required")
    @Min(value = 0, message = "Experience cannot be negative")
    @Max(value = 70, message = "Experience must be 70 years or less")
    private Integer experienceYears;

    @NotNull(message = "Consultation fee is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "Consultation fee cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Consultation fee must have at most 2 decimal places")
    private BigDecimal consultationFee;

    @NotBlank(message = "Clinic or hospital address is required")
    @Size(max = 255, message = "Clinic address must be at most 255 characters")
    private String clinicAddress;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone number must be 10 to 15 digits and may start with +")
    private String phoneNumber;

    @Size(max = 2000, message = "Biography must be at most 2000 characters")
    private String biography;

    @Size(max = 40, message = "A doctor may publish at most 40 availability slots")
    private List<@Size(max = 40, message = "Each availability slot must be at most 40 characters") String> availabilitySlots = new ArrayList<>();

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

    public String getQualifications() {
        return qualifications;
    }

    public void setQualifications(String qualifications) {
        this.qualifications = qualifications;
    }

    public Integer getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(Integer experienceYears) {
        this.experienceYears = experienceYears;
    }

    public BigDecimal getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(BigDecimal consultationFee) {
        this.consultationFee = consultationFee;
    }

    public String getClinicAddress() {
        return clinicAddress;
    }

    public void setClinicAddress(String clinicAddress) {
        this.clinicAddress = clinicAddress;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public List<String> getAvailabilitySlots() {
        return availabilitySlots;
    }

    public void setAvailabilitySlots(List<String> availabilitySlots) {
        this.availabilitySlots = availabilitySlots;
    }
}
