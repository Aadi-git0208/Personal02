package com.medicurex.profile.dto;

import com.medicurex.profile.model.ApprovalStatus;
import jakarta.validation.constraints.NotNull;

public class ApprovalRequest {

    @NotNull(message = "Approval status is required")
    private ApprovalStatus status;

    public ApprovalStatus getStatus() {
        return status;
    }

    public void setStatus(ApprovalStatus status) {
        this.status = status;
    }
}
