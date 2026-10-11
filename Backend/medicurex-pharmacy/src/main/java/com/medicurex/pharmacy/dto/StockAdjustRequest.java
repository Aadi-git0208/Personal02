package com.medicurex.pharmacy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class StockAdjustRequest {

    @NotNull(message = "Stock delta is required")
    private Integer delta;

    @NotBlank(message = "Operation key is required")
    @Size(max = 180, message = "Operation key must be at most 180 characters")
    private String operationKey;

    public Integer getDelta() {
        return delta;
    }

    public void setDelta(Integer delta) {
        this.delta = delta;
    }

    public String getOperationKey() {
        return operationKey;
    }

    public void setOperationKey(String operationKey) {
        this.operationKey = operationKey;
    }
}
