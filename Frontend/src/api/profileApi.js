import { apiRequest } from "./client";

export function getDoctorProfile(token) {
    return apiRequest("/api/profiles/doctor/me", {
        method: "GET",
        token,
        skipAuthRedirect: true
    });
}

export function saveDoctorProfile(payload) {
    return apiRequest("/api/profiles/doctor/me", {
        method: "PUT",
        body: payload
    });
}

export function getDoctorDirectory() {
    return apiRequest("/api/profiles/doctor/directory", {
        method: "GET",
        auth: false
    });
}

export function getDoctorDirectoryEntry(doctorProfileId) {
    return apiRequest(`/api/profiles/doctor/directory/${doctorProfileId}`, {
        method: "GET",
        auth: false
    });
}

export function getPatientProfile() {
    return apiRequest("/api/profiles/patient/me", {
        method: "GET"
    });
}

export function savePatientProfile(payload) {
    return apiRequest("/api/profiles/patient/me", {
        method: "PUT",
        body: payload
    });
}

export function getAdminDirectory() {
    return apiRequest("/api/profiles/admin", {
        method: "GET"
    });
}

export function getAdminUserProfiles(userId) {
    return apiRequest(`/api/profiles/admin/${userId}`, {
        method: "GET"
    });
}

export function updateDoctorApproval(userId, status) {
    return apiRequest(`/api/profiles/admin/${userId}/approval`, {
        method: "PATCH",
        body: { status }
    });
}
