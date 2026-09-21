import React, { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { getApiErrorMessage } from "../api/client";
import { useAuth } from "../auth/AuthContext";
import { getDashboardPath } from "../auth/session";
import "./Login.css";

function Login() {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [rememberMe, setRememberMe] = useState(false);
    const [error, setError] = useState("");
    const [submitting, setSubmitting] = useState(false);

    const navigate = useNavigate();
    const location = useLocation();
    const { login } = useAuth();
    const registered = Boolean(location.state?.registered);

    async function handleLogin(event) {
        event.preventDefault();
        setError("");
        setSubmitting(true);

        try {
            const user = await login(email.trim(), password);
            navigate(getDashboardPath(user), { replace: true });
        } catch (loginError) {
            setError(getApiErrorMessage(loginError));
        } finally {
            setSubmitting(false);
        }
    }

    return (
        <div className="auth-page">
            <div className="auth-card">
                <div className="auth-header">
                    <h2 className="auth-title">
                        Welcome Back
                    </h2>

                    <p className="auth-subtitle">
                        Login to continue to{" "}
                        <span className="auth-subtitle__brand">
                            MediCurex
                        </span>
                    </p>
                </div>

                {registered && !error && (
                    <p className="auth-banner auth-banner--success" role="status">
                        Registration successful. Please log in.
                    </p>
                )}

                {error && (
                    <p className="auth-banner auth-banner--error" role="alert">
                        {error}
                    </p>
                )}

                <form
                    className="auth-form"
                    onSubmit={handleLogin}
                >
                    <div className="form-group">
                        <label
                            htmlFor="email"
                            className="form-label"
                        >
                            Email Address
                        </label>

                        <input
                            id="email"
                            type="email"
                            className="form-input"
                            placeholder="Enter your email"
                            value={email}
                            onChange={(event) =>
                                setEmail(event.target.value)
                            }
                            autoComplete="email"
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label
                            htmlFor="password"
                            className="form-label"
                        >
                            Password
                        </label>

                        <input
                            id="password"
                            type="password"
                            className="form-input"
                            placeholder="Enter your password"
                            value={password}
                            onChange={(event) =>
                                setPassword(event.target.value)
                            }
                            autoComplete="current-password"
                            required
                        />
                    </div>

                    <div className="form-options">
                        <label className="checkbox-label">
                            <input
                                type="checkbox"
                                checked={rememberMe}
                                onChange={(event) =>
                                    setRememberMe(
                                        event.target.checked
                                    )
                                }
                                className="checkbox-input"
                            />

                            <span>Remember me</span>
                        </label>

                        <Link
                            to="#"
                            className="form-link"
                        >
                            Forgot Password?
                        </Link>
                    </div>

                    <button
                        type="submit"
                        className="auth-button auth-button--primary"
                        disabled={submitting}
                    >
                        {submitting ? "Logging in..." : "Login"}
                    </button>
                </form>

                <p className="auth-footer">
                    Don't have an account?{" "}
                    <Link
                        to="/register"
                        className="auth-footer__link"
                    >
                        Register
                    </Link>
                </p>
            </div>
        </div>
    );
}

export default Login;
