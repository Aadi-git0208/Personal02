package com.medicurex.pharmacy.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class StockUpdateRequest {

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer quantity;

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
