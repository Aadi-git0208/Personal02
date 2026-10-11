package com.medicurex.appointment.dto;

import com.medicurex.appointment.model.Appointment;
import com.medicurex.appointment.model.AppointmentStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public class AppointmentResponse {

    private Long id;
    private Long patientUserId;
    private Long doctorUserId;
    private Long doctorProfileId;
    private String doctorFullName;
    private String doctorSpecialization;
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String slotLabel;
    private String reason;
    private String notes;
    private AppointmentStatus status;
    private String cancellationReason;
    private Instant createdAt;
    private Instant updatedAt;

    public static AppointmentResponse from(Appointment appointment, boolean includeNotes) {
        AppointmentResponse response = new AppointmentResponse();
        response.id = appointment.getId();
        response.patientUserId = appointment.getPatientUserId();
        response.doctorUserId = appointment.getDoctorUserId();
        response.doctorProfileId = appointment.getDoctorProfileId();
        response.doctorFullName = appointment.getDoctorFullName();
        response.doctorSpecialization = appointment.getDoctorSpecialization();
        response.appointmentDate = appointment.getAppointmentDate();
        response.startTime = appointment.getStartTime();
        response.endTime = appointment.getEndTime();
        response.slotLabel = appointment.getSlotLabel();
        response.reason = appointment.getReason();
        response.notes = includeNotes ? appointment.getNotes() : null;
        response.status = appointment.getStatus();
        response.cancellationReason = appointment.getCancellationReason();
        response.createdAt = appointment.getCreatedAt();
        response.updatedAt = appointment.getUpdatedAt();
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

    public Long getDoctorProfileId() {
        return doctorProfileId;
    }

    public String getDoctorFullName() {
        return doctorFullName;
    }

    public String getDoctorSpecialization() {
        return doctorSpecialization;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public String getSlotLabel() {
        return slotLabel;
    }

    public String getReason() {
        return reason;
    }

    public String getNotes() {
        return notes;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
