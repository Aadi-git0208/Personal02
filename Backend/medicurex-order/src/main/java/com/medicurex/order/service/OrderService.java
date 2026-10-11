package com.medicurex.order.service;

import com.medicurex.order.client.CatalogLookupService;
import com.medicurex.order.client.MedicineSnapshot;
import com.medicurex.order.client.StockReservationService;
import com.medicurex.order.dto.CancelOrderRequest;
import com.medicurex.order.dto.CreateOrderRequest;
import com.medicurex.order.dto.OrderItemRequest;
import com.medicurex.order.dto.OrderResponse;
import com.medicurex.order.dto.StatusUpdateRequest;
import com.medicurex.order.exception.ApiException;
import com.medicurex.order.model.OrderItem;
import com.medicurex.order.model.OrderStatus;
import com.medicurex.order.model.PharmacyOrder;
import com.medicurex.order.repository.OrderRepository;
import com.medicurex.order.security.AuthenticatedUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class OrderService {

    private static final Map<OrderStatus, Set<OrderStatus>> ADMIN_TRANSITIONS = new EnumMap<>(OrderStatus.class);

    static {
        ADMIN_TRANSITIONS.put(OrderStatus.PENDING, Set.of(OrderStatus.CONFIRMED, OrderStatus.REJECTED));
        ADMIN_TRANSITIONS.put(OrderStatus.CONFIRMED, Set.of(OrderStatus.PROCESSING, OrderStatus.CANCELLED));
        ADMIN_TRANSITIONS.put(OrderStatus.PROCESSING, Set.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED));
        ADMIN_TRANSITIONS.put(OrderStatus.SHIPPED, Set.of(OrderStatus.DELIVERED));
    }

    private final OrderRepository orderRepository;
    private final CatalogLookupService catalogLookupService;
    private final StockReservationService stockReservationService;
    private final BigDecimal deliveryFee;

    public OrderService(
            OrderRepository orderRepository,
            CatalogLookupService catalogLookupService,
            StockReservationService stockReservationService,
            @Value("${app.order.delivery-fee:0.00}") BigDecimal deliveryFee
    ) {
        this.orderRepository = orderRepository;
        this.catalogLookupService = catalogLookupService;
        this.stockReservationService = stockReservationService;
        this.deliveryFee = deliveryFee.setScale(2, RoundingMode.HALF_UP);
    }

    public OrderResponse create(AuthenticatedUser user, CreateOrderRequest request) {
        if (!user.isPatient()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only patients can create orders");
        }

        List<OrderItemRequest> merged = mergeItems(request.getItems());
        String payloadHash = hashPayload(merged, request);
        String idempotencyKey = blankToNull(request.getIdempotencyKey());
        if (idempotencyKey != null) {
            var existing = orderRepository.findByPatientUserIdAndIdempotencyKey(user.getUserId(), idempotencyKey);
            if (existing.isPresent()) {
                PharmacyOrder order = existing.get();
                if (!payloadHash.equals(order.getPayloadHash())) {
                    throw new ApiException(HttpStatus.CONFLICT, "Idempotency key was already used for a different checkout");
                }
                return OrderResponse.from(orderRepository.findWithItemsById(order.getId()).orElse(order));
            }
        }

        List<PreparedLine> lines = new ArrayList<>();
        for (OrderItemRequest item : merged) {
            MedicineSnapshot medicine = catalogLookupService.requireOrderable(item.getMedicineId());
            BigDecimal unitPrice = medicine.getUnitPrice().setScale(2, RoundingMode.HALF_UP);
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())).setScale(2, RoundingMode.HALF_UP);
            lines.add(new PreparedLine(medicine, item.getQuantity(), unitPrice, lineTotal));
        }
        lines.sort(Comparator.comparing(line -> line.medicine().getId()));

        BigDecimal subtotal = lines.stream()
                .map(PreparedLine::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(deliveryFee).setScale(2, RoundingMode.HALF_UP);
        String scope = idempotencyKey != null ? idempotencyKey : UUID.randomUUID().toString();

        List<PreparedLine> reserved = new ArrayList<>();
        try {
            for (PreparedLine line : lines) {
                stockReservationService.reserve(
                        line.medicine().getId(),
                        line.quantity(),
                        reservationKey(user.getUserId(), scope, line.medicine().getId())
                );
                reserved.add(line);
            }

            PharmacyOrder order = new PharmacyOrder();
            order.setPatientUserId(user.getUserId());
            order.setStatus(OrderStatus.CONFIRMED);
            order.setSubtotal(subtotal);
            order.setDeliveryFee(deliveryFee);
            order.setTotalAmount(total);
            order.setRecipientName(request.getRecipientName().trim());
            order.setPhone(request.getPhone().trim());
            order.setAddress(request.getAddress().trim());
            order.setInstructions(blankToNull(request.getInstructions()));
            order.setIdempotencyKey(idempotencyKey);
            order.setPayloadHash(payloadHash);
            order.setReservationScope(scope);
            order.setStockReserved(true);
            for (PreparedLine line : lines) {
                order.addItem(toItem(line));
            }
            return OrderResponse.from(orderRepository.save(order));
        } catch (RuntimeException exception) {
            for (PreparedLine line : reserved) {
                stockReservationService.releaseQuietly(
                        line.medicine().getId(),
                        reservationKey(user.getUserId(), scope, line.medicine().getId())
                );
            }
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> listMine(AuthenticatedUser user, Pageable pageable) {
        return orderRepository
                .findByPatientUserIdOrderByCreatedAtDesc(user.getUserId(), pageable)
                .map(OrderResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> listAll(Pageable pageable) {
        return orderRepository.findAllByOrderByCreatedAtDesc(pageable).map(OrderResponse::from);
    }

    @Transactional(readOnly = true)
    public OrderResponse getForPatient(AuthenticatedUser user, Long orderId) {
        PharmacyOrder order = requireWithItems(orderId);
        if (!order.isOwnedBy(user.getUserId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getForAdmin(Long orderId) {
        return OrderResponse.from(requireWithItems(orderId));
    }

    @Transactional
    public OrderResponse cancel(AuthenticatedUser user, Long orderId, CancelOrderRequest request) {
        PharmacyOrder order = orderRepository.lockById(orderId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));
        if (!order.isOwnedBy(user.getUserId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access denied");
        }
        if (!order.isCancellable()) {
            throw new ApiException(HttpStatus.CONFLICT, "Order cannot be cancelled in its current status");
        }
        releaseIfReserved(order);
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(Instant.now());
        order.setCancellationReason(blankToNull(request == null ? null : request.getReason()));
        order.setStockReserved(false);
        return OrderResponse.from(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse updateStatus(Long orderId, StatusUpdateRequest request) {
        PharmacyOrder order = orderRepository.lockById(orderId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));
        Set<OrderStatus> allowed = ADMIN_TRANSITIONS.getOrDefault(order.getStatus(), Set.of());
        if (!allowed.contains(request.getStatus())) {
            throw new ApiException(HttpStatus.CONFLICT, "Invalid order status transition");
        }
        if (request.getStatus() == OrderStatus.CANCELLED || request.getStatus() == OrderStatus.REJECTED) {
            releaseIfReserved(order);
            order.setCancelledAt(Instant.now());
            order.setStockReserved(false);
        }
        order.setStatus(request.getStatus());
        return OrderResponse.from(orderRepository.save(order));
    }

    private void releaseIfReserved(PharmacyOrder order) {
        if (!order.isStockReserved()) {
            return;
        }
        PharmacyOrder withItems = orderRepository.findWithItemsById(order.getId()).orElse(order);
        for (OrderItem item : withItems.getItems()) {
            stockReservationService.release(
                    item.getMedicineId(),
                    reservationKey(order.getPatientUserId(), order.getReservationScope(), item.getMedicineId())
            );
        }
    }

    private PharmacyOrder requireWithItems(Long orderId) {
        return orderRepository.findWithItemsById(orderId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    private List<OrderItemRequest> mergeItems(List<OrderItemRequest> items) {
        Map<Long, Integer> quantities = new LinkedHashMap<>();
        for (OrderItemRequest item : items) {
            quantities.merge(item.getMedicineId(), item.getQuantity(), Integer::sum);
        }
        List<OrderItemRequest> merged = new ArrayList<>();
        quantities.forEach((medicineId, quantity) -> {
            if (quantity > 100) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Quantity must be at most 100");
            }
            OrderItemRequest request = new OrderItemRequest();
            request.setMedicineId(medicineId);
            request.setQuantity(quantity);
            merged.add(request);
        });
        merged.sort(Comparator.comparing(OrderItemRequest::getMedicineId));
        return merged;
    }

    private OrderItem toItem(PreparedLine line) {
        OrderItem item = new OrderItem();
        item.setMedicineId(line.medicine().getId());
        item.setMedicineName(line.medicine().getName());
        item.setStrength(line.medicine().getStrength());
        item.setDosageForm(line.medicine().getDosageForm());
        item.setUnitPrice(line.unitPrice());
        item.setQuantity(line.quantity());
        item.setLineTotal(line.lineTotal());
        return item;
    }

    private String reservationKey(Long patientId, String scope, Long medicineId) {
        return "reserve:" + patientId + ":" + scope + ":" + medicineId;
    }

    private String hashPayload(List<OrderItemRequest> items, CreateOrderRequest request) {
        StringBuilder builder = new StringBuilder();
        for (OrderItemRequest item : items) {
            builder.append(item.getMedicineId()).append(':').append(item.getQuantity()).append('|');
        }
        builder.append(request.getRecipientName().trim()).append('|')
                .append(request.getPhone().trim()).append('|')
                .append(request.getAddress().trim()).append('|')
                .append(request.getInstructions() == null ? "" : request.getInstructions().trim());
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(builder.toString().getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is required", exception);
        }
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private record PreparedLine(MedicineSnapshot medicine, int quantity, BigDecimal unitPrice, BigDecimal lineTotal) {
    }
}
