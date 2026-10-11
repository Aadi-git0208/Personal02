package com.medicurex.order.controller;

import com.medicurex.order.dto.CancelOrderRequest;
import com.medicurex.order.dto.CreateOrderRequest;
import com.medicurex.order.dto.OrderResponse;
import com.medicurex.order.dto.StatusUpdateRequest;
import com.medicurex.order.security.SecurityUtils;
import com.medicurex.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(
            @Valid @RequestBody CreateOrderRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyHeader
    ) {
        if ((request.getIdempotencyKey() == null || request.getIdempotencyKey().isBlank())
                && idempotencyHeader != null && !idempotencyHeader.isBlank()) {
            request.setIdempotencyKey(idempotencyHeader.trim());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.create(SecurityUtils.currentUser(), request));
    }

    @GetMapping("/my")
    public Page<OrderResponse> mine(@PageableDefault(size = 20) Pageable pageable) {
        return orderService.listMine(SecurityUtils.currentUser(), pageable);
    }

    @GetMapping
    public Page<OrderResponse> adminList(@PageableDefault(size = 20) Pageable pageable) {
        return orderService.listAll(pageable);
    }

    @GetMapping("/{orderId}")
    public OrderResponse get(@PathVariable Long orderId) {
        return orderService.getForPatient(SecurityUtils.currentUser(), orderId);
    }

    @GetMapping("/{orderId}/admin")
    public OrderResponse adminGet(@PathVariable Long orderId) {
        return orderService.getForAdmin(orderId);
    }

    @PatchMapping("/{orderId}/cancel")
    public OrderResponse cancel(
            @PathVariable Long orderId,
            @RequestBody(required = false) @Valid CancelOrderRequest request
    ) {
        return orderService.cancel(SecurityUtils.currentUser(), orderId, request);
    }

    @PatchMapping("/{orderId}/status")
    public OrderResponse updateStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody StatusUpdateRequest request
    ) {
        return orderService.updateStatus(orderId, request);
    }
}
