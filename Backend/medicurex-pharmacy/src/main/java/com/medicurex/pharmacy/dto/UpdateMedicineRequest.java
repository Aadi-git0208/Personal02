package com.medicurex.pharmacy.dto;

import com.medicurex.pharmacy.model.DosageForm;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class UpdateMedicineRequest {

    @NotBlank(message = "Medicine name is required")
    @Size(max = 160, message = "Medicine name must be at most 160 characters")
    private String name;

    @Size(max = 160, message = "Generic name must be at most 160 characters")
    private String genericName;

    @Size(max = 160, message = "Manufacturer must be at most 160 characters")
    private String manufacturer;

    @Size(max = 2000, message = "Description must be at most 2000 characters")
    private String description;

    @NotNull(message = "Dosage form is required")
    private DosageForm dosageForm;

    @NotBlank(message = "Strength is required")
    @Size(max = 80, message = "Strength must be at most 80 characters")
    private String strength;

    @NotBlank(message = "Category is required")
    @Size(max = 80, message = "Category must be at most 80 characters")
    private String category;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.00", message = "Unit price cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Unit price must use a decimal-safe format")
    private BigDecimal unitPrice;

    private Boolean prescriptionRequired;

    @Size(max = 500, message = "Image URL must be at most 500 characters")
    private String imageUrl;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGenericName() {
        return genericName;
    }

    public void setGenericName(String genericName) {
        this.genericName = genericName;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DosageForm getDosageForm() {
        return dosageForm;
    }

    public void setDosageForm(DosageForm dosageForm) {
        this.dosageForm = dosageForm;
    }

    public String getStrength() {
        return strength;
    }

    public void setStrength(String strength) {
        this.strength = strength;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Boolean getPrescriptionRequired() {
        return prescriptionRequired;
    }

    public void setPrescriptionRequired(Boolean prescriptionRequired) {
        this.prescriptionRequired = prescriptionRequired;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
