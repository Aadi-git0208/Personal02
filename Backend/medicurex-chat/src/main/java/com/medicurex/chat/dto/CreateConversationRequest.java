package com.medicurex.chat.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Positive;

public class CreateConversationRequest {

    @Positive
    private Long appointmentId;

    @Positive
    private Long doctorProfileId;

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public Long getDoctorProfileId() {
        return doctorProfileId;
    }

    public void setDoctorProfileId(Long doctorProfileId) {
        this.doctorProfileId = doctorProfileId;
    }

    @AssertTrue(message = "Provide appointmentId or doctorProfileId")
    public boolean isTargetPresent() {
        return appointmentId != null || doctorProfileId != null;
    }
}
