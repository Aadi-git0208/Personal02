package com.apexcare.prescription.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class CreatePrescriptionRequest {

    @NotNull(message = "Appointment id is required")
    private Long appointmentId;

    @NotBlank(message = "Diagnosis is required")
    @Size(max = 500, message = "Diagnosis must be at most 500 characters")
    private String diagnosis;

    @Size(max = 2000, message = "Notes must be at most 2000 characters")
    private String notes;

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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<PrescriptionItemRequest> getItems() {
        return items;
    }

    public void setItems(List<PrescriptionItemRequest> items) {
        this.items = items;
    }
}
