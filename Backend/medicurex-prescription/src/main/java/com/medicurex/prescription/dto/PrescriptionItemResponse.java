package com.medicurex.prescription.dto;

import com.medicurex.prescription.model.PrescriptionItem;

import java.math.BigDecimal;

public class PrescriptionItemResponse {

    private Long id;
    private String medicineName;
    private String dosage;
    private String route;
    private String frequency;
    private String duration;
    private BigDecimal quantity;
    private String instructions;

    public static PrescriptionItemResponse from(PrescriptionItem item) {
        PrescriptionItemResponse response = new PrescriptionItemResponse();
        response.id = item.getId();
        response.medicineName = item.getMedicineName();
        response.dosage = item.getDosage();
        response.route = item.getRoute();
        response.frequency = item.getFrequency();
        response.duration = item.getDuration();
        response.quantity = item.getQuantity();
        response.instructions = item.getInstructions();
        return response;
    }

    public Long getId() {
        return id;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public String getDosage() {
        return dosage;
    }

    public String getRoute() {
        return route;
    }

    public String getFrequency() {
        return frequency;
    }

    public String getDuration() {
        return duration;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public String getInstructions() {
        return instructions;
    }
}
