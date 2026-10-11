import { createContext, useContext, useEffect, useMemo, useState } from "react";
import { getCurrentUser, loginUser, registerUser } from "../api/authApi";
import { ApiError } from "../api/client";
import { getDoctorProfile } from "../api/profileApi";
import {
    clearSession,
    getDashboardPath,
    getStoredToken,
    getStoredUser,
    saveSession
} from "./session";

const AuthContext = createContext(null);

async function withDoctorProfile(user, token) {
    if (!user || user.role !== "doctor") {
        return user;
    }

    try {
        const profile = await getDoctorProfile(token);
        return {
            ...user,
            profileCompleted: Boolean(profile?.profileCompleted),
            approvalStatus: profile?.approvalStatus,
            doctorProfileId: profile?.id
        };
    } catch (error) {
        if (error instanceof ApiError && error.status === 404) {
            return {
                ...user,
                profileCompleted: false,
                approvalStatus: null,
                doctorProfileId: null
            };
        }

        return {
            ...user,
            profileCompleted: Boolean(user.profileCompleted),
            profileLookupFailed: true
        };
    }
}

export function AuthProvider({ children }) {
    const [user, setUser] = useState(() => getStoredUser());
    const [token, setToken] = useState(() => getStoredToken());
    const [loading, setLoading] = useState(() => Boolean(getStoredToken()));

    useEffect(() => {
        let cancelled = false;

        async function restoreSession() {
            const storedToken = getStoredToken();

            if (!storedToken) {
                if (getStoredUser()) {
                    clearSession();
                }
                if (!cancelled) {
                    setUser(null);
                    setToken(null);
                    setLoading(false);
                }
                return;
            }

            try {
                const currentUser = await getCurrentUser(storedToken);
                const enrichedUser = await withDoctorProfile(currentUser, storedToken);
                if (cancelled) {
                    return;
                }
                saveSession(storedToken, enrichedUser);
                setUser(enrichedUser);
                setToken(storedToken);
            } catch {
                if (cancelled) {
                    return;
                }
                clearSession();
                setUser(null);
                setToken(null);
            } finally {
                if (!cancelled) {
                    setLoading(false);
                }
            }
        }

        restoreSession();

        return () => {
            cancelled = true;
        };
    }, []);

    const value = useMemo(() => ({
        user,
        token,
        loading,
        isAuthenticated: Boolean(user && token),
        async register(payload) {
            return registerUser(payload);
        },
        async login(email, password) {
            const response = await loginUser({ email, password });
            const enrichedUser = await withDoctorProfile(response.user, response.token);
            saveSession(response.token, enrichedUser);
            setToken(response.token);
            setUser(enrichedUser);
            return enrichedUser;
        },
        logout() {
            clearSession();
            setToken(null);
            setUser(null);
        },
        async refreshUser() {
            const storedToken = getStoredToken();
            if (!storedToken) {
                return null;
            }
            const currentUser = await getCurrentUser(storedToken);
            const enrichedUser = await withDoctorProfile(currentUser, storedToken);
            saveSession(storedToken, enrichedUser);
            setUser(enrichedUser);
            setToken(storedToken);
            return enrichedUser;
        },
        getDashboardPath
    }), [loading, token, user]);

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth() {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error("useAuth must be used within an AuthProvider");
    }
    return context;
}
