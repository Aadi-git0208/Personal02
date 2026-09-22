package com.apexcare.order.controller;

import com.apexcare.order.dto.OrderResponse;
import com.apexcare.order.dto.UpdateOrderStatusRequest;
import com.apexcare.order.security.AuthenticatedUser;
import com.apexcare.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkout(
            Authentication authentication,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.checkout(AuthenticatedUser.from(authentication), authorization));
    }

    @GetMapping("/my-orders")
    public ResponseEntity<List<OrderResponse>> myOrders(Authentication authentication) {
        return ResponseEntity.ok(orderService.listMine(AuthenticatedUser.from(authentication)));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> listAll() {
        return ResponseEntity.ok(orderService.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> get(Authentication authentication, @PathVariable Long id) {
        return ResponseEntity.ok(orderService.get(AuthenticatedUser.from(authentication), id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(
            Authentication authentication,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        AuthenticatedUser.from(authentication);
        return ResponseEntity.ok(orderService.updateStatus(id, request, authorization));
    }
}
