package com.medicurex.order.dto;

import com.medicurex.order.model.OrderItem;

import java.math.BigDecimal;

public class OrderItemResponse {

    private Long id;
    private Long medicineId;
    private String medicineName;
    private String strength;
    private String dosageForm;
    private BigDecimal unitPrice;
    private int quantity;
    private BigDecimal lineTotal;

    public static OrderItemResponse from(OrderItem item) {
        OrderItemResponse response = new OrderItemResponse();
        response.id = item.getId();
        response.medicineId = item.getMedicineId();
        response.medicineName = item.getMedicineName();
        response.strength = item.getStrength();
        response.dosageForm = item.getDosageForm();
        response.unitPrice = item.getUnitPrice();
        response.quantity = item.getQuantity();
        response.lineTotal = item.getLineTotal();
        return response;
    }

    public Long getId() {
        return id;
    }

    public Long getMedicineId() {
        return medicineId;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public String getStrength() {
        return strength;
    }

    public String getDosageForm() {
        return dosageForm;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }
}
