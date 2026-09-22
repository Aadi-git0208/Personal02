package com.apexcare.order.service;

import com.apexcare.order.client.MedicineSnapshot;
import com.apexcare.order.client.PharmacyClient;
import com.apexcare.order.dto.UpdateOrderStatusRequest;
import com.apexcare.order.entity.Cart;
import com.apexcare.order.entity.CartItem;
import com.apexcare.order.entity.OrderStatus;
import com.apexcare.order.entity.PharmacyOrder;
import com.apexcare.order.exception.ApiException;
import com.apexcare.order.repository.CartRepository;
import com.apexcare.order.repository.OrderRepository;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private PharmacyClient pharmacyClient;

    private CartService cartService;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        cartService = new CartService(cartRepository, pharmacyClient);
        orderService = new OrderService(orderRepository, cartRepository, cartService, pharmacyClient);
    }

    @Test
    void checkoutUsesPharmacyPriceAndClearsCart() {
        Cart cart = cartWithItem(2L, 2);
        when(cartRepository.findGraphByPatientId(11L)).thenReturn(Optional.of(cart));
        when(pharmacyClient.getMedicine(2L, "Bearer token")).thenReturn(medicine(2L, "Paracetamol", "50.00", 20));
        when(pharmacyClient.adjustStock(2L, -2, "Bearer token")).thenReturn(medicine(2L, "Paracetamol", "50.00", 18));
        when(orderRepository.save(any(PharmacyOrder.class))).thenAnswer(invocation -> {
            PharmacyOrder order = invocation.getArgument(0);
            order.setId(9L);
            return order;
        });
        when(cartRepository.save(cart)).thenReturn(cart);

        var response = orderService.checkout(patient(), "Bearer token");

        assertThat(response.getTotalAmount()).isEqualByComparingTo("100.00");
        assertThat(response.getItems().getFirst().getPriceSnapshot()).isEqualByComparingTo("50.00");
        assertThat(response.getStatus()).isEqualTo(OrderStatus.PLACED);
        assertThat(cart.getItems()).isEmpty();
        verify(pharmacyClient).adjustStock(2L, -2, "Bearer token");
    }

    @Test
    void checkoutRestoresStockWhenInventoryUpdateFailsMidway() {
        Cart cart = new Cart();
        cart.setPatientId(11L);
        CartItem first = new CartItem();
        first.setMedicineId(2L);
        first.setQuantity(1);
        CartItem second = new CartItem();
        second.setMedicineId(3L);
        second.setQuantity(1);
        cart.addItem(first);
        cart.addItem(second);
        when(cartRepository.findGraphByPatientId(11L)).thenReturn(Optional.of(cart));
        when(pharmacyClient.getMedicine(2L, "Bearer token")).thenReturn(medicine(2L, "Paracetamol", "50.00", 5));
        when(pharmacyClient.getMedicine(3L, "Bearer token")).thenReturn(medicine(3L, "Ibuprofen", "40.00", 5));
        when(pharmacyClient.adjustStock(2L, -1, "Bearer token")).thenReturn(medicine(2L, "Paracetamol", "50.00", 4));
        when(pharmacyClient.adjustStock(eq(3L), eq(-1), eq("Bearer token")))
                .thenThrow(new ApiException(HttpStatus.CONFLICT, "Insufficient stock"));
        when(pharmacyClient.adjustStock(2L, 1, "Bearer token")).thenReturn(medicine(2L, "Paracetamol", "50.00", 5));

        assertThatThrownBy(() -> orderService.checkout(patient(), "Bearer token"))
                .isInstanceOf(ApiException.class);
        verify(pharmacyClient).adjustStock(2L, 1, "Bearer token");
        verify(orderRepository, never()).save(any());
    }

    @Test
    void adminCannotSkipPlacedToShipped() {
        PharmacyOrder order = new PharmacyOrder();
        order.setId(9L);
        order.setPatientId(11L);
        order.setStatus(OrderStatus.PLACED);
        order.setTotalAmount(new BigDecimal("100.00"));
        when(orderRepository.findGraphById(9L)).thenReturn(Optional.of(order));

        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
        request.setStatus(OrderStatus.SHIPPED);

        assertThatThrownBy(() -> orderService.updateStatus(9L, request, "Bearer admin"))
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
        item.setMedicineId(medicineId);
        item.setQuantity(quantity);
        cart.addItem(item);
        return cart;
    }
}
