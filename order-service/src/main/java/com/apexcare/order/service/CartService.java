package com.apexcare.order.service;

import com.apexcare.order.client.MedicineSnapshot;
import com.apexcare.order.client.PharmacyClient;
import com.apexcare.order.dto.CartItemRequest;
import com.apexcare.order.dto.CartItemResponse;
import com.apexcare.order.dto.CartResponse;
import com.apexcare.order.dto.UpdateCartItemRequest;
import com.apexcare.order.entity.Cart;
import com.apexcare.order.entity.CartItem;
import com.apexcare.order.exception.ApiException;
import com.apexcare.order.repository.CartRepository;
import com.apexcare.order.security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final PharmacyClient pharmacyClient;

    public CartService(CartRepository cartRepository, PharmacyClient pharmacyClient) {
        this.cartRepository = cartRepository;
        this.pharmacyClient = pharmacyClient;
    }

    @Transactional
    public CartResponse getOrCreate(AuthenticatedUser user, String authorization) {
        Cart cart = requireCart(user.getUserId());
        return toResponse(cart, authorization);
    }

    @Transactional
    public CartResponse addItem(AuthenticatedUser user, String authorization, CartItemRequest request) {
        Cart cart = requireCart(user.getUserId());
        MedicineSnapshot medicine = requireAvailable(request.getMedicineId(), authorization);
        CartItem existing = cart.getItems().stream()
                .filter(item -> item.getMedicineId().equals(request.getMedicineId()))
                .findFirst()
                .orElse(null);
        int nextQuantity = request.getQuantity() + (existing == null ? 0 : existing.getQuantity());
        assertStock(medicine, nextQuantity);
        if (existing == null) {
            CartItem item = new CartItem();
            item.setMedicineId(request.getMedicineId());
            item.setQuantity(request.getQuantity());
            cart.addItem(item);
        } else {
            existing.setQuantity(nextQuantity);
        }
        cart.touch();
        return toResponse(cartRepository.save(cart), authorization);
    }

    @Transactional
    public CartResponse updateItem(AuthenticatedUser user, String authorization, Long medicineId, UpdateCartItemRequest request) {
        Cart cart = requireCart(user.getUserId());
        CartItem item = cart.getItems().stream()
                .filter(candidate -> candidate.getMedicineId().equals(medicineId))
                .findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Cart item not found"));
        MedicineSnapshot medicine = requireAvailable(medicineId, authorization);
        assertStock(medicine, request.getQuantity());
        item.setQuantity(request.getQuantity());
        cart.touch();
        return toResponse(cartRepository.save(cart), authorization);
    }

    @Transactional
    public CartResponse removeItem(AuthenticatedUser user, String authorization, Long medicineId) {
        Cart cart = requireCart(user.getUserId());
        boolean removed = cart.getItems().removeIf(item -> item.getMedicineId().equals(medicineId));
        if (!removed) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Cart item not found");
        }
        cart.touch();
        return toResponse(cartRepository.save(cart), authorization);
    }

    @Transactional
    public CartResponse clear(AuthenticatedUser user, String authorization) {
        Cart cart = requireCart(user.getUserId());
        cart.getItems().clear();
        cart.touch();
        return toResponse(cartRepository.save(cart), authorization);
    }

    Cart requireLoaded(Long patientId) {
        return cartRepository.findGraphByPatientId(patientId)
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setPatientId(patientId);
                    return cartRepository.save(cart);
                });
    }

    private Cart requireCart(Long patientId) {
        return requireLoaded(patientId);
    }

    MedicineSnapshot requireAvailable(Long medicineId, String authorization) {
        MedicineSnapshot medicine = pharmacyClient.getMedicine(medicineId, authorization);
        if (Boolean.FALSE.equals(medicine.getActive())) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Medicine not found");
        }
        return medicine;
    }

    void assertStock(MedicineSnapshot medicine, int quantity) {
        if (medicine.getStockQuantity() < quantity) {
            throw new ApiException(HttpStatus.CONFLICT, "Insufficient stock for " + medicine.getName());
        }
    }

    CartResponse toResponse(Cart cart, String authorization) {
        List<CartItemResponse> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        for (CartItem item : cart.getItems()) {
            MedicineSnapshot medicine = requireAvailable(item.getMedicineId(), authorization);
            BigDecimal price = medicine.getPrice().setScale(2, RoundingMode.HALF_UP);
            BigDecimal subtotal = price.multiply(BigDecimal.valueOf(item.getQuantity())).setScale(2, RoundingMode.HALF_UP);
            CartItemResponse response = new CartItemResponse();
            response.setId(item.getId());
            response.setMedicineId(item.getMedicineId());
            response.setMedicineName(medicine.getName());
            response.setQuantity(item.getQuantity());
            response.setPrice(price);
            response.setStockQuantity(medicine.getStockQuantity());
            response.setSubtotal(subtotal);
            items.add(response);
            total = total.add(subtotal);
        }
        CartResponse response = new CartResponse();
        response.setId(cart.getId());
        response.setPatientId(cart.getPatientId());
        response.setItems(items);
        response.setTotalAmount(total);
        response.setCreatedAt(cart.getCreatedAt());
        response.setUpdatedAt(cart.getUpdatedAt());
        return response;
    }
}
