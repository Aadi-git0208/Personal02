import { apiRequest } from "./client";

export function createPrescription(payload) {
    return apiRequest("/api/prescriptions", {
        method: "POST",
        body: payload
    });
}

export function listDoctorPrescriptions(query) {
    return apiRequest("/api/prescriptions/doctor/me", {
        method: "GET",
        query
    });
}

export function listPatientPrescriptions(query) {
    return apiRequest("/api/prescriptions/patient/me", {
        method: "GET",
        query
    });
}

export function getPatientPrescription(prescriptionId) {
    return apiRequest(`/api/prescriptions/patient/me/${prescriptionId}`, {
        method: "GET"
    });
}

export function getPrescription(prescriptionId) {
    return apiRequest(`/api/prescriptions/${prescriptionId}`, {
        method: "GET"
    });
}

export function cancelPrescription(prescriptionId, reason) {
    return apiRequest(`/api/prescriptions/${prescriptionId}/cancel`, {
        method: "PATCH",
        body: reason ? { reason } : {}
    });
}
