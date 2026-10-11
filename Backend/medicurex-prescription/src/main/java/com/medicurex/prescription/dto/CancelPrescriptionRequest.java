package com.medicurex.prescription.dto;

import jakarta.validation.constraints.Size;

public class CancelPrescriptionRequest {

    @Size(max = 255, message = "Cancellation reason must be at most 255 characters")
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
