import { apiRequest } from "./client";

export function listMedicines(query) {
    return apiRequest("/api/medicines", {
        method: "GET",
        query,
        auth: true
    });
}

export function searchMedicines(query) {
    return apiRequest("/api/medicines/search", {
        method: "GET",
        query,
        auth: true
    });
}

export function listMedicineCategories() {
    return apiRequest("/api/medicines/categories", {
        method: "GET",
        auth: true
    });
}

export function getMedicine(medicineId) {
    return apiRequest(`/api/medicines/${medicineId}`, {
        method: "GET",
        auth: true
    });
}

export function createMedicine(payload) {
    return apiRequest("/api/medicines", {
        method: "POST",
        body: payload
    });
}

export function updateMedicine(medicineId, payload) {
    return apiRequest(`/api/medicines/${medicineId}`, {
        method: "PUT",
        body: payload
    });
}

export function updateMedicineStatus(medicineId, status) {
    return apiRequest(`/api/medicines/${medicineId}/status`, {
        method: "PATCH",
        body: { status }
    });
}

export function updateMedicineStock(medicineId, quantity) {
    return apiRequest(`/api/medicines/${medicineId}/stock`, {
        method: "PATCH",
        body: { quantity }
    });
}
