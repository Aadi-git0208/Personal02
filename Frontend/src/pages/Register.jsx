import React, { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { getApiErrorMessage } from "../api/client";
import { useAuth } from "../auth/AuthContext";
import "./Register.css";

function Register() {
    const navigate = useNavigate();
    const { register } = useAuth();

    const [name, setName] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [role, setRole] = useState("patient");
    const [error, setError] = useState("");
    const [submitting, setSubmitting] = useState(false);

    async function handleRegister(event) {
        event.preventDefault();
        setError("");

        if (password !== confirmPassword) {
            setError("Passwords do not match. Please try again.");
            return;
        }

        setSubmitting(true);

        try {
            await register({
                name: name.trim(),
                email: email.trim(),
                password,
                role
            });

            navigate("/login", {
                replace: true,
                state: { registered: true }
            });
        } catch (registerError) {
            setError(getApiErrorMessage(registerError));
        } finally {
            setSubmitting(false);
        }
    }

    return (
        <div className="auth-page">
            <div className="auth-card">

                <div className="auth-header">
                    <h2 className="auth-title">
                        Create Account
                    </h2>

                    <p className="auth-subtitle">
                        Join{" "}
                        <span className="auth-subtitle__brand">
                            MediCurex
                        </span>{" "}
                        today
                    </p>
                </div>

                {error && (
                    <p className="auth-banner auth-banner--error" role="alert">
                        {error}
                    </p>
                )}

                <form
                    className="auth-form"
                    onSubmit={handleRegister}
                >
                    <div className="form-group">
                        <label
                            htmlFor="name"
                            className="form-label"
                        >
                            Full Name
                        </label>

                        <input
                            id="name"
                            type="text"
                            className="form-input"
                            placeholder="Enter your full name"
                            value={name}
                            onChange={(event) =>
                                setName(event.target.value)
                            }
                            autoComplete="name"
                            minLength={2}
                            required
                        />
                    </div>

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
                            htmlFor="role"
                            className="form-label"
                        >
                            Register As
                        </label>

                        <select
                            id="role"
                            className="form-input"
                            value={role}
                            onChange={(event) =>
                                setRole(event.target.value)
                            }
                            required
                        >
                            <option value="patient">
                                Patient
                            </option>

                            <option value="doctor">
                                Doctor
                            </option>
                        </select>
                        <p className="form-hint">
                            Admin accounts are created by the system, not public registration.
                        </p>
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
                            placeholder="Create a password"
                            value={password}
                            onChange={(event) =>
                                setPassword(event.target.value)
                            }
                            autoComplete="new-password"
                            minLength={6}
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label
                            htmlFor="confirmPassword"
                            className="form-label"
                        >
                            Confirm Password
                        </label>

                        <input
                            id="confirmPassword"
                            type="password"
                            className="form-input"
                            placeholder="Confirm your password"
                            value={confirmPassword}
                            onChange={(event) =>
                                setConfirmPassword(event.target.value)
                            }
                            autoComplete="new-password"
                            minLength={6}
                            required
                        />
                    </div>

                    <button
                        type="submit"
                        className="auth-button auth-button--primary"
                        disabled={submitting}
                    >
                        {submitting ? "Creating account..." : "Register"}
                    </button>
                </form>

                <p className="auth-footer">
                    Already have an account?{" "}
                    <Link
                        to="/login"
                        className="auth-footer__link"
                    >
                        Login
                    </Link>
                </p>

            </div>
        </div>
    );
}

export default Register;
