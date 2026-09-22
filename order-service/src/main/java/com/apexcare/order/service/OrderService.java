package com.apexcare.order.service;

import com.apexcare.order.client.MedicineSnapshot;
import com.apexcare.order.client.PharmacyClient;
import com.apexcare.order.dto.OrderItemResponse;
import com.apexcare.order.dto.OrderResponse;
import com.apexcare.order.dto.UpdateOrderStatusRequest;
import com.apexcare.order.entity.Cart;
import com.apexcare.order.entity.CartItem;
import com.apexcare.order.entity.OrderItem;
import com.apexcare.order.entity.OrderStatus;
import com.apexcare.order.entity.PharmacyOrder;
import com.apexcare.order.exception.ApiException;
import com.apexcare.order.repository.CartRepository;
import com.apexcare.order.repository.OrderRepository;
import com.apexcare.order.security.AuthenticatedUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartService cartService;
    private final PharmacyClient pharmacyClient;

    public OrderService(
            OrderRepository orderRepository,
            CartRepository cartRepository,
            CartService cartService,
            PharmacyClient pharmacyClient
    ) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.cartService = cartService;
        this.pharmacyClient = pharmacyClient;
    }

    @Transactional
    public OrderResponse checkout(AuthenticatedUser user, String authorization) {
        Cart cart = cartService.requireLoaded(user.getUserId());
        if (cart.getItems().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cart is empty");
        }

        List<PreparedLine> lines = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        for (CartItem item : cart.getItems()) {
            MedicineSnapshot medicine = cartService.requireAvailable(item.getMedicineId(), authorization);
            cartService.assertStock(medicine, item.getQuantity());
            BigDecimal price = medicine.getPrice().setScale(2, RoundingMode.HALF_UP);
            BigDecimal subtotal = price.multiply(BigDecimal.valueOf(item.getQuantity())).setScale(2, RoundingMode.HALF_UP);
            lines.add(new PreparedLine(item.getMedicineId(), medicine.getName(), item.getQuantity(), price, subtotal));
            total = total.add(subtotal);
        }

        List<PreparedLine> decremented = new ArrayList<>();
        try {
            for (PreparedLine line : lines) {
                pharmacyClient.adjustStock(line.medicineId(), -line.quantity(), authorization);
                decremented.add(line);
            }

            PharmacyOrder order = new PharmacyOrder();
            order.setPatientId(user.getUserId());
            order.setStatus(OrderStatus.PLACED);
            order.setTotalAmount(total);
            for (PreparedLine line : lines) {
                OrderItem orderItem = new OrderItem();
                orderItem.setMedicineId(line.medicineId());
                orderItem.setMedicineName(line.medicineName());
                orderItem.setQuantity(line.quantity());
                orderItem.setPriceSnapshot(line.price());
                orderItem.setSubtotal(line.subtotal());
                order.addItem(orderItem);
            }
            PharmacyOrder saved = orderRepository.save(order);
            cart.getItems().clear();
            cart.touch();
            cartRepository.save(cart);
            return toResponse(saved);
        } catch (RuntimeException exception) {
            restoreStock(decremented, authorization);
            if (exception instanceof ApiException) {
                throw exception;
            }
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Checkout failed");
        }
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> listMine(AuthenticatedUser user) {
        return orderRepository.findGraphByPatientId(user.getUserId()).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> listAll() {
        return orderRepository.findAllGraph().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse get(AuthenticatedUser user, Long id) {
        PharmacyOrder order = orderRepository.findGraphById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));
        if ("PATIENT".equals(user.getRole()) && !user.getUserId().equals(order.getPatientId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return toResponse(order);
    }

    @Transactional
    public OrderResponse updateStatus(Long id, UpdateOrderStatusRequest request, String authorization) {
        PharmacyOrder order = orderRepository.findGraphById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));
        OrderStatus next = request.getStatus();
        if (!allowedTransition(order.getStatus(), next)) {
            throw new ApiException(HttpStatus.CONFLICT, "Cannot change status from " + order.getStatus() + " to " + next);
        }
        if (next == OrderStatus.CANCELLED && order.getStatus() != OrderStatus.CANCELLED) {
            restoreOrderStock(order, authorization);
        }
        order.setStatus(next);
        return toResponse(orderRepository.save(order));
    }

    private void restoreOrderStock(PharmacyOrder order, String authorization) {
        for (OrderItem item : order.getItems()) {
            try {
                pharmacyClient.adjustStock(item.getMedicineId(), item.getQuantity(), authorization);
            } catch (RuntimeException exception) {
                log.error("Failed to restore stock for medicine {} after cancel", item.getMedicineId(), exception);
                throw new ApiException(HttpStatus.BAD_GATEWAY, "Unable to restore stock while cancelling");
            }
        }
    }

    private void restoreStock(List<PreparedLine> decremented, String authorization) {
        for (PreparedLine line : decremented) {
            try {
                pharmacyClient.adjustStock(line.medicineId(), line.quantity(), authorization);
            } catch (RuntimeException restoreException) {
                log.error("Failed to restore stock for medicine {} after checkout failure", line.medicineId(), restoreException);
            }
        }
    }

    private boolean allowedTransition(OrderStatus current, OrderStatus next) {
        if (current == next) {
            return false;
        }
        return switch (current) {
            case PLACED -> Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED).contains(next);
            case CONFIRMED -> Set.of(OrderStatus.PACKED, OrderStatus.CANCELLED).contains(next);
            case PACKED -> Set.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED).contains(next);
            case SHIPPED -> next == OrderStatus.DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }

    private OrderResponse toResponse(PharmacyOrder order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setPatientId(order.getPatientId());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        response.setItems(order.getItems().stream().map(item -> {
            OrderItemResponse itemResponse = new OrderItemResponse();
            itemResponse.setId(item.getId());
            itemResponse.setOrderId(order.getId());
            itemResponse.setMedicineId(item.getMedicineId());
            itemResponse.setMedicineName(item.getMedicineName());
            itemResponse.setQuantity(item.getQuantity());
            itemResponse.setPriceSnapshot(item.getPriceSnapshot());
            itemResponse.setSubtotal(item.getSubtotal());
            return itemResponse;
        }).toList());
        return response;
    }

    private record PreparedLine(Long medicineId, String medicineName, int quantity, BigDecimal price, BigDecimal subtotal) {
    }
}
