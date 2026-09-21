const TOKEN_KEY = "token";
const USER_KEY = "currentUser";
const LOGGED_IN_KEY = "isLoggedIn";

export function getStoredToken() {
    return localStorage.getItem(TOKEN_KEY);
}

export function getStoredUser() {
    try {
        const raw = localStorage.getItem(USER_KEY);
        return raw ? JSON.parse(raw) : null;
    } catch {
        return null;
    }
}

export function saveSession(token, user) {
    if (token) {
        localStorage.setItem(TOKEN_KEY, token);
    }

    localStorage.setItem(USER_KEY, JSON.stringify(user));
    localStorage.setItem(LOGGED_IN_KEY, "true");
    window.dispatchEvent(new Event("userLogin"));
}

export function clearSession() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    localStorage.removeItem(LOGGED_IN_KEY);
    window.dispatchEvent(new Event("userLogout"));
}

export function hasDoctorProfile(user) {
    if (!user?.id) {
        return false;
    }

    try {
        const profiles = JSON.parse(localStorage.getItem("doctorProfiles")) || [];
        return profiles.some(
            (profile) =>
                String(profile.id) === String(user.id) ||
                String(profile.userId) === String(user.id)
        );
    } catch {
        return false;
    }
}

export function getDashboardPath(user) {
    if (!user?.role) {
        return "/";
    }

    if (user.role === "admin") {
        return "/admin/dashboard";
    }

    if (user.role === "doctor") {
        return hasDoctorProfile(user) ? "/doctor" : "/doctor/onboarding";
    }

    return "/patient/dashboard";
}
