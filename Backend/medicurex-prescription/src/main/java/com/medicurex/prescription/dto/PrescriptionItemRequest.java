package com.medicurex.prescription.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class PrescriptionItemRequest {

    @NotBlank(message = "Medicine name is required")
    @Size(max = 200, message = "Medicine name must be at most 200 characters")
    private String medicineName;

    @NotBlank(message = "Dosage is required")
    @Size(max = 100, message = "Dosage must be at most 100 characters")
    private String dosage;

    @Size(max = 50, message = "Route must be at most 50 characters")
    private String route;

    @NotBlank(message = "Frequency is required")
    @Size(max = 100, message = "Frequency must be at most 100 characters")
    private String frequency;

    @NotBlank(message = "Duration is required")
    @Size(max = 100, message = "Duration must be at most 100 characters")
    private String duration;

    @DecimalMin(value = "0.01", message = "Quantity must be greater than zero")
    @Digits(integer = 10, fraction = 2, message = "Quantity must use a decimal-safe format")
    private BigDecimal quantity;

    @Size(max = 500, message = "Item instructions must be at most 500 characters")
    private String instructions;

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }
}
