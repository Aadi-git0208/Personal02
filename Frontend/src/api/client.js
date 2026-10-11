const API_BASE = import.meta.env.VITE_API_URL || "";

const AUTH_ENDPOINTS_WITHOUT_REDIRECT = new Set([
    "/api/auth/login",
    "/api/auth/register",
    "/api/auth/me"
]);

const STATUS_MESSAGES = {
    401: "Your session has expired. Please sign in again.",
    403: "You do not have permission to do that.",
    404: "The requested record was not found.",
    409: "This action conflicts with the current data.",
    502: "A required service is temporarily unavailable. Please try again.",
    503: "A required service is temporarily unavailable. Please try again."
};

let authRedirectInFlight = false;

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

        if (STATUS_MESSAGES[error.status]) {
            return STATUS_MESSAGES[error.status];
        }

        return error.message;
    }

    if (error instanceof TypeError) {
        return "Cannot connect to the server. Make sure the backend is running.";
    }

    return error?.message || "Something went wrong. Please try again.";
}

export function pageContent(page) {
    if (Array.isArray(page)) {
        return page;
    }

    return Array.isArray(page?.content) ? page.content : [];
}

function buildQueryString(query) {
    if (!query || typeof query !== "object") {
        return "";
    }

    const params = new URLSearchParams();

    Object.entries(query).forEach(([key, value]) => {
        if (value === undefined || value === null || value === "") {
            return;
        }

        if (Array.isArray(value)) {
            value.forEach((entry) => {
                if (entry !== undefined && entry !== null && entry !== "") {
                    params.append(key, String(entry));
                }
            });
            return;
        }

        params.set(key, String(value));
    });

    const serialized = params.toString();
    return serialized ? `?${serialized}` : "";
}

function pathWithoutQuery(path) {
    return String(path || "").split("?")[0];
}

function clearStoredSession() {
    localStorage.removeItem("token");
    localStorage.removeItem("currentUser");
    localStorage.removeItem("isLoggedIn");
    window.dispatchEvent(new Event("userLogout"));
}

function redirectToLogin() {
    if (authRedirectInFlight) {
        return;
    }

    const pathname = window.location.pathname;
    if (pathname === "/login" || pathname === "/register") {
        return;
    }

    authRedirectInFlight = true;
    window.location.assign("/login");
}

export async function apiRequest(path, options = {}) {
    const {
        method = "GET",
        body,
        token,
        query,
        headers: extraHeaders,
        auth = true,
        skipAuthRedirect = false,
        idempotencyKey
    } = options;

    const headers = {
        Accept: "application/json",
        ...(extraHeaders || {})
    };

    if (body !== undefined) {
        headers["Content-Type"] = "application/json";
    }

    if (idempotencyKey) {
        headers["Idempotency-Key"] = idempotencyKey;
    }

    if (auth) {
        const authToken = token ?? localStorage.getItem("token");
        if (authToken) {
            headers.Authorization = `Bearer ${authToken}`;
        }
    }

    const url = `${API_BASE}${path}${buildQueryString(query)}`;

    let response;
    try {
        response = await fetch(url, {
            method,
            headers,
            body: body !== undefined ? JSON.stringify(body) : undefined
        });
    } catch {
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
        const message =
            data?.message ||
            STATUS_MESSAGES[response.status] ||
            "Request failed";

        if (
            response.status === 401 &&
            !skipAuthRedirect &&
            !AUTH_ENDPOINTS_WITHOUT_REDIRECT.has(pathWithoutQuery(path))
        ) {
            clearStoredSession();
            redirectToLogin();
        }

        throw new ApiError(message, response.status, data);
    }

    return data;
}
