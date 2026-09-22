package com.apexcare.order.service;

import com.apexcare.order.client.MedicineSnapshot;
import com.apexcare.order.client.PharmacyClient;
import com.apexcare.order.dto.CartItemRequest;
import com.apexcare.order.dto.UpdateCartItemRequest;
import com.apexcare.order.entity.Cart;
import com.apexcare.order.entity.CartItem;
import com.apexcare.order.exception.ApiException;
import com.apexcare.order.repository.CartRepository;
import com.apexcare.order.security.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private PharmacyClient pharmacyClient;

    private CartService cartService;

    @BeforeEach
    void setUp() {
        cartService = new CartService(cartRepository, pharmacyClient);
    }

    @Test
    void createsCartAndAddsItemUsingPharmacyStock() {
        AuthenticatedUser patient = patient();
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setPatientId(11L);
        when(cartRepository.findGraphByPatientId(11L)).thenReturn(Optional.of(cart));
        when(pharmacyClient.getMedicine(2L, "Bearer token")).thenReturn(medicine(2L, "Paracetamol", "50.00", 20));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartItemRequest request = new CartItemRequest();
        request.setMedicineId(2L);
        request.setQuantity(2);

        var response = cartService.addItem(patient, "Bearer token", request);

        assertThat(response.getPatientId()).isEqualTo(11L);
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().getFirst().getQuantity()).isEqualTo(2);
        assertThat(response.getTotalAmount()).isEqualByComparingTo("100.00");
    }

    @Test
    void updatesQuantity() {
        AuthenticatedUser patient = patient();
        Cart cart = cartWithItem(2L, 1);
        when(cartRepository.findGraphByPatientId(11L)).thenReturn(Optional.of(cart));
        when(pharmacyClient.getMedicine(2L, "Bearer token")).thenReturn(medicine(2L, "Paracetamol", "50.00", 20));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(3);

        var response = cartService.updateItem(patient, "Bearer token", 2L, request);

        assertThat(response.getItems().getFirst().getQuantity()).isEqualTo(3);
        assertThat(response.getTotalAmount()).isEqualByComparingTo("150.00");
    }

    @Test
    void rejectsQuantityAboveStock() {
        AuthenticatedUser patient = patient();
        Cart cart = new Cart();
        cart.setPatientId(11L);
        when(cartRepository.findGraphByPatientId(11L)).thenReturn(Optional.of(cart));
        when(pharmacyClient.getMedicine(2L, "Bearer token")).thenReturn(medicine(2L, "Paracetamol", "50.00", 1));

        CartItemRequest request = new CartItemRequest();
        request.setMedicineId(2L);
        request.setQuantity(5);

        assertThatThrownBy(() -> cartService.addItem(patient, "Bearer token", request))
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> assertThat(((ApiException) exception).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    private static AuthenticatedUser patient() {
        return new AuthenticatedUser(11L, "rahul@gmail.com", "patient");
    }

    private static MedicineSnapshot medicine(Long id, String name, String price, int stock) {
        MedicineSnapshot snapshot = new MedicineSnapshot();
        snapshot.setId(id);
        snapshot.setName(name);
        snapshot.setPrice(new BigDecimal(price));
        snapshot.setStockQuantity(stock);
        snapshot.setActive(true);
        return snapshot;
    }

    private static Cart cartWithItem(Long medicineId, int quantity) {
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setPatientId(11L);
        CartItem item = new CartItem();
        item.setId(8L);
        item.setMedicineId(medicineId);
        item.setQuantity(quantity);
        cart.addItem(item);
        return cart;
    }
}
