import { createContext, useContext, useEffect, useMemo, useState } from "react";
import { getCurrentUser, loginUser, registerUser } from "../api/authApi";
import {
    clearSession,
    getDashboardPath,
    getStoredToken,
    getStoredUser,
    saveSession
} from "./session";

const AuthContext = createContext(null);

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
                if (cancelled) {
                    return;
                }
                saveSession(storedToken, currentUser);
                setUser(currentUser);
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
            saveSession(response.token, response.user);
            setToken(response.token);
            setUser(response.user);
            return response.user;
        },
        logout() {
            clearSession();
            setToken(null);
            setUser(null);
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
