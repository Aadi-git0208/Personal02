package com.medicurex.pharmacy.dto;

import com.medicurex.pharmacy.model.AvailabilityStatus;
import com.medicurex.pharmacy.model.DosageForm;
import com.medicurex.pharmacy.model.Medicine;

import java.math.BigDecimal;
import java.time.Instant;

public class MedicineResponse {

    private Long id;
    private String name;
    private String genericName;
    private String manufacturer;
    private String description;
    private DosageForm dosageForm;
    private String strength;
    private String category;
    private BigDecimal unitPrice;
    private int stockQuantity;
    private AvailabilityStatus status;
    private boolean prescriptionRequired;
    private String imageUrl;
    private Instant createdAt;
    private Instant updatedAt;

    public static MedicineResponse from(Medicine medicine) {
        MedicineResponse response = new MedicineResponse();
        response.id = medicine.getId();
        response.name = medicine.getName();
        response.genericName = medicine.getGenericName();
        response.manufacturer = medicine.getManufacturer();
        response.description = medicine.getDescription();
        response.dosageForm = medicine.getDosageForm();
        response.strength = medicine.getStrength();
        response.category = medicine.getCategory();
        response.unitPrice = medicine.getUnitPrice();
        response.stockQuantity = medicine.getStockQuantity();
        response.status = medicine.getStatus();
        response.prescriptionRequired = medicine.isPrescriptionRequired();
        response.imageUrl = medicine.getImageUrl();
        response.createdAt = medicine.getCreatedAt();
        response.updatedAt = medicine.getUpdatedAt();
        return response;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getGenericName() {
        return genericName;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getDescription() {
        return description;
    }

    public DosageForm getDosageForm() {
        return dosageForm;
    }

    public String getStrength() {
        return strength;
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public AvailabilityStatus getStatus() {
        return status;
    }

    public boolean isPrescriptionRequired() {
        return prescriptionRequired;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
