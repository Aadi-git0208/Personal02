package com.medicurex.prescription.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public class CreatePrescriptionRequest {

    @NotNull(message = "Appointment ID is required")
    @Positive
    private Long appointmentId;

    @NotBlank(message = "Diagnosis is required")
    @Size(max = 1000, message = "Diagnosis must be at most 1000 characters")
    private String diagnosis;

    @Size(max = 2000, message = "Instructions must be at most 2000 characters")
    private String instructions;

    @FutureOrPresent(message = "Expiry date cannot be in the past")
    private LocalDate expiresOn;

    @NotEmpty(message = "At least one prescription item is required")
    @Valid
    private List<PrescriptionItemRequest> items;

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public LocalDate getExpiresOn() {
        return expiresOn;
    }

    public void setExpiresOn(LocalDate expiresOn) {
        this.expiresOn = expiresOn;
    }

    public List<PrescriptionItemRequest> getItems() {
        return items;
    }

    public void setItems(List<PrescriptionItemRequest> items) {
        this.items = items;
    }
}
