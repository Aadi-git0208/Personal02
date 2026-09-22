package com.apexcare.order.controller;

import com.apexcare.order.dto.CartItemRequest;
import com.apexcare.order.dto.CartResponse;
import com.apexcare.order.dto.UpdateCartItemRequest;
import com.apexcare.order.security.AuthenticatedUser;
import com.apexcare.order.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<CartResponse> get(
            Authentication authentication,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) {
        return ResponseEntity.ok(cartService.getOrCreate(AuthenticatedUser.from(authentication), authorization));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(
            Authentication authentication,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @Valid @RequestBody CartItemRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cartService.addItem(AuthenticatedUser.from(authentication), authorization, request));
    }

    @PutMapping("/items/{medicineId}")
    public ResponseEntity<CartResponse> updateItem(
            Authentication authentication,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @PathVariable Long medicineId,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        return ResponseEntity.ok(
                cartService.updateItem(AuthenticatedUser.from(authentication), authorization, medicineId, request)
        );
    }

    @DeleteMapping("/items/{medicineId}")
    public ResponseEntity<CartResponse> removeItem(
            Authentication authentication,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @PathVariable Long medicineId
    ) {
        return ResponseEntity.ok(
                cartService.removeItem(AuthenticatedUser.from(authentication), authorization, medicineId)
        );
    }

    @DeleteMapping
    public ResponseEntity<CartResponse> clear(
            Authentication authentication,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) {
        return ResponseEntity.ok(cartService.clear(AuthenticatedUser.from(authentication), authorization));
    }
}
