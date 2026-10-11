package com.medicurex.pharmacy.dto;

import com.medicurex.pharmacy.model.AvailabilityStatus;
import jakarta.validation.constraints.NotNull;

public class StatusChangeRequest {

    @NotNull(message = "Status is required")
    private AvailabilityStatus status;

    public AvailabilityStatus getStatus() {
        return status;
    }

    public void setStatus(AvailabilityStatus status) {
        this.status = status;
    }
}
