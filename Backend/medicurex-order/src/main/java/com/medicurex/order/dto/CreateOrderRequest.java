package com.medicurex.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public class CreateOrderRequest {

    @NotEmpty(message = "At least one order item is required")
    @Size(max = 20, message = "An order can contain at most 20 items")
    @Valid
    private List<OrderItemRequest> items;

    @NotBlank(message = "Recipient name is required")
    @Size(max = 100)
    private String recipientName;

    @NotBlank(message = "Phone is required")
    @Size(min = 8, max = 20, message = "Phone must be between 8 and 20 characters")
    private String phone;

    @NotBlank(message = "Delivery address is required")
    @Size(max = 500)
    private String address;

    @Size(max = 500)
    private String instructions;

    @Size(max = 80)
    private String idempotencyKey;

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }
}
