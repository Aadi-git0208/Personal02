package com.apexcare.pharmacy.dto;

import jakarta.validation.constraints.Min;

public class UpdateStockRequest {

    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stockQuantity;

    private Integer adjustment;

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public Integer getAdjustment() {
        return adjustment;
    }

    public void setAdjustment(Integer adjustment) {
        this.adjustment = adjustment;
    }
}
