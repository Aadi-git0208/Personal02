package com.medicurex.pharmacy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class StockReleaseRequest {

    @NotBlank(message = "Operation key is required")
    @Size(max = 180, message = "Operation key must be at most 180 characters")
    private String operationKey;

    public String getOperationKey() {
        return operationKey;
    }

    public void setOperationKey(String operationKey) {
        this.operationKey = operationKey;
    }
}
