import { apiRequest } from "./client";

export function createOrder(payload, idempotencyKey) {
    return apiRequest("/api/orders", {
        method: "POST",
        body: payload,
        idempotencyKey
    });
}

export function listMyOrders(query) {
    return apiRequest("/api/orders/my", {
        method: "GET",
        query
    });
}

export function listAdminOrders(query) {
    return apiRequest("/api/orders", {
        method: "GET",
        query
    });
}

export function getMyOrder(orderId) {
    return apiRequest(`/api/orders/${orderId}`, {
        method: "GET"
    });
}

export function getAdminOrder(orderId) {
    return apiRequest(`/api/orders/${orderId}/admin`, {
        method: "GET"
    });
}

export function cancelOrder(orderId, reason) {
    return apiRequest(`/api/orders/${orderId}/cancel`, {
        method: "PATCH",
        body: reason ? { reason } : {}
    });
}

export function updateOrderStatus(orderId, status) {
    return apiRequest(`/api/orders/${orderId}/status`, {
        method: "PATCH",
        body: { status }
    });
}
