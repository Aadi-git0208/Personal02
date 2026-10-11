package com.medicurex.prescription.dto;

import com.medicurex.prescription.model.Prescription;
import com.medicurex.prescription.model.PrescriptionStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public class PrescriptionResponse {

    private Long id;
    private Long patientUserId;
    private Long doctorUserId;
    private Long appointmentId;
    private String diagnosis;
    private String instructions;
    private PrescriptionStatus status;
    private LocalDate expiresOn;
    private String cancellationReason;
    private Instant cancelledAt;
    private Instant createdAt;
    private Instant updatedAt;
    private List<PrescriptionItemResponse> items;

    public static PrescriptionResponse from(Prescription prescription) {
        PrescriptionResponse response = new PrescriptionResponse();
        response.id = prescription.getId();
        response.patientUserId = prescription.getPatientUserId();
        response.doctorUserId = prescription.getDoctorUserId();
        response.appointmentId = prescription.getAppointmentId();
        response.diagnosis = prescription.getDiagnosis();
        response.instructions = prescription.getInstructions();
        response.status = prescription.getStatus();
        response.expiresOn = prescription.getExpiresOn();
        response.cancellationReason = prescription.getCancellationReason();
        response.cancelledAt = prescription.getCancelledAt();
        response.createdAt = prescription.getCreatedAt();
        response.updatedAt = prescription.getUpdatedAt();
        response.items = prescription.getItems().stream().map(PrescriptionItemResponse::from).toList();
        return response;
    }

    public Long getId() {
        return id;
    }

    public Long getPatientUserId() {
        return patientUserId;
    }

    public Long getDoctorUserId() {
        return doctorUserId;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public String getInstructions() {
        return instructions;
    }

    public PrescriptionStatus getStatus() {
        return status;
    }

    public LocalDate getExpiresOn() {
        return expiresOn;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<PrescriptionItemResponse> getItems() {
        return items;
    }
}
