package com.medicurex.appointment.dto;

import jakarta.validation.constraints.Size;

public class StatusChangeRequest {

    @Size(max = 255, message = "Reason must be at most 255 characters")
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
