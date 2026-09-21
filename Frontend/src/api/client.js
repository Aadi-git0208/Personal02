const API_BASE = import.meta.env.VITE_API_URL || "";

export class ApiError extends Error {
    constructor(message, status, data) {
        super(message);
        this.name = "ApiError";
        this.status = status;
        this.data = data;
    }
}

export function getApiErrorMessage(error) {
    if (error instanceof ApiError) {
        const fieldErrors = error.data?.fieldErrors;
        if (fieldErrors && typeof fieldErrors === "object") {
            const firstFieldError = Object.values(fieldErrors).find(Boolean);
            if (firstFieldError) {
                return String(firstFieldError);
            }
        }

        if (error.data?.message) {
            return error.data.message;
        }

        return error.message;
    }

    if (error instanceof TypeError) {
        return "Cannot connect to the server. Make sure the backend is running.";
    }

    return error?.message || "Something went wrong. Please try again.";
}

export async function apiRequest(path, options = {}) {
    const { method = "GET", body, token } = options;
    const headers = {
        Accept: "application/json"
    };

    if (body !== undefined) {
        headers["Content-Type"] = "application/json";
    }

    const authToken = token ?? localStorage.getItem("token");
    if (authToken) {
        headers.Authorization = `Bearer ${authToken}`;
    }

    let response;
    try {
        response = await fetch(`${API_BASE}${path}`, {
            method,
            headers,
            body: body !== undefined ? JSON.stringify(body) : undefined
        });
    } catch (error) {
        throw new ApiError(
            "Cannot connect to the server. Make sure the backend is running.",
            0,
            null
        );
    }

    const text = await response.text();
    let data = null;

    if (text) {
        try {
            data = JSON.parse(text);
        } catch {
            data = { message: text };
        }
    }

    if (!response.ok) {
        throw new ApiError(
            data?.message || "Request failed",
            response.status,
            data
        );
    }

    return data;
}
