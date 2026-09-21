import { apiRequest } from "./client";

export function registerUser(payload) {
    return apiRequest("/api/auth/register", {
        method: "POST",
        body: payload
    });
}

export function loginUser(payload) {
    return apiRequest("/api/auth/login", {
        method: "POST",
        body: payload
    });
}

export function getCurrentUser(token) {
    return apiRequest("/api/auth/me", {
        method: "GET",
        token
    });
}
