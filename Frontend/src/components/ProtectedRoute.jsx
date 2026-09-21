import { Navigate, useLocation } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import { getDashboardPath } from "../auth/session";

function AuthLoadingScreen() {
    return (
        <div className="auth-page">
            <div className="auth-card">
                <p className="auth-subtitle">Checking your session...</p>
            </div>
        </div>
    );
}

export function ProtectedRoute({ children, roles }) {
    const { user, loading } = useAuth();
    const location = useLocation();

    if (loading) {
        return <AuthLoadingScreen />;
    }

    if (!user) {
        return (
            <Navigate
                to="/login"
                replace
                state={{ from: location.pathname }}
            />
        );
    }

    if (roles && !roles.includes(user.role)) {
        return <Navigate to={getDashboardPath(user)} replace />;
    }

    return children;
}

export function GuestRoute({ children }) {
    const { user, loading } = useAuth();

    if (loading) {
        return <AuthLoadingScreen />;
    }

    if (user) {
        return <Navigate to={getDashboardPath(user)} replace />;
    }

    return children;
}
