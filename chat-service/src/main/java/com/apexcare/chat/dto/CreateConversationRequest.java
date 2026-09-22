package com.apexcare.chat.dto;

import jakarta.validation.constraints.NotNull;

public class CreateConversationRequest {

    @NotNull(message = "Appointment id is required")
    private Long appointmentId;

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }
}
