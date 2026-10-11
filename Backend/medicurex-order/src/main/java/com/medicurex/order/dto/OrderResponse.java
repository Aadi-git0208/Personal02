package com.medicurex.order.dto;

import com.medicurex.order.model.OrderStatus;
import com.medicurex.order.model.PharmacyOrder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class OrderResponse {

    private Long id;
    private Long patientUserId;
    private OrderStatus status;
    private BigDecimal subtotal;
    private BigDecimal deliveryFee;
    private BigDecimal totalAmount;
    private String recipientName;
    private String phone;
    private String address;
    private String instructions;
    private String cancellationReason;
    private Instant cancelledAt;
    private Instant createdAt;
    private Instant updatedAt;
    private List<OrderItemResponse> items;

    public static OrderResponse from(PharmacyOrder order) {
        OrderResponse response = new OrderResponse();
        response.id = order.getId();
        response.patientUserId = order.getPatientUserId();
        response.status = order.getStatus();
        response.subtotal = order.getSubtotal();
        response.deliveryFee = order.getDeliveryFee();
        response.totalAmount = order.getTotalAmount();
        response.recipientName = order.getRecipientName();
        response.phone = order.getPhone();
        response.address = order.getAddress();
        response.instructions = order.getInstructions();
        response.cancellationReason = order.getCancellationReason();
        response.cancelledAt = order.getCancelledAt();
        response.createdAt = order.getCreatedAt();
        response.updatedAt = order.getUpdatedAt();
        response.items = order.getItems().stream().map(OrderItemResponse::from).toList();
        return response;
    }

    public Long getId() {
        return id;
    }

    public Long getPatientUserId() {
        return patientUserId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getDeliveryFee() {
        return deliveryFee;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public String getInstructions() {
        return instructions;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<OrderItemResponse> getItems() {
        return items;
    }
}
