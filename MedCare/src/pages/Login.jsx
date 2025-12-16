import React, { useState } from "react";
import { Link } from "react-router-dom";
import "./Login.css";

function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [rememberMe, setRememberMe] = useState(false);

  function handleLogin(e) {
    e.preventDefault();

    if (email === "test@gmail.com" && password === "123456") {
      alert("Login Successful! Welcome to MediCurex");
    } else {
      alert("Account not found. Please register first.");
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-header">
          <h2 className="auth-title">Welcome Back</h2>
          <p className="auth-subtitle">
            Login to continue to <span className="auth-subtitle__brand">MediCurex</span>
          </p>
        </div>

        <form className="auth-form" onSubmit={handleLogin}>
          <div className="form-group">
            <label htmlFor="email" className="form-label">Email Address</label>
            <input
              id="email"
              type="email"
              className="form-input"
              placeholder="Enter your email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="password" className="form-label">Password</label>
            <input
              id="password"
              type="password"
              className="form-input"
              placeholder="Enter your password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>

          <div className="form-options">
            <label className="checkbox-label">
              <input 
                type="checkbox" 
                checked={rememberMe}
                onChange={(e) => setRememberMe(e.target.checked)}
                className="checkbox-input"
              />
              <span>Remember me</span>
            </label>
            <Link to="#" className="form-link">Forgot Password?</Link>
          </div>

          <button type="submit" className="auth-button auth-button--primary">
            Login
          </button>
        </form>

        <p className="auth-footer">
          Don't have an account? <Link to="/register" className="auth-footer__link">Register</Link>
        </p>
      </div>
    </div>
  );
}

export default Login;
