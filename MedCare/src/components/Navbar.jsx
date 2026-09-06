import React, { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import "./Navbar.css";

const Navbar = ({ darkMode, setDarkMode }) => {
  const [currentUser, setCurrentUser] = useState(() => {
    try {
      const user = localStorage.getItem("currentUser");
      return user ? JSON.parse(user) : null;
    } catch {
      return null;
    }
  });

  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    const handleStorageChange = () => {
      try {
        const user = localStorage.getItem("currentUser");
        setCurrentUser(user ? JSON.parse(user) : null);
      } catch {
        setCurrentUser(null);
      }
    };

    window.addEventListener("storage", handleStorageChange);
    window.addEventListener("userLogin", handleStorageChange);
    window.addEventListener("userLogout", handleStorageChange);

    return () => {
      window.removeEventListener("storage", handleStorageChange);
      window.removeEventListener("userLogin", handleStorageChange);
      window.removeEventListener("userLogout", handleStorageChange);
    };
  }, []);

  const toggleMobileMenu = () => {
    setIsMobileMenuOpen(!isMobileMenuOpen);
  };

  const closeMobileMenu = () => {
    setIsMobileMenuOpen(false);
  };

  const toggleTheme = () => {
    setDarkMode((previousMode) => !previousMode);
  };

  const handleLogout = () => {
    localStorage.removeItem("currentUser");
    localStorage.removeItem("isLoggedIn");
    setCurrentUser(null);
    window.dispatchEvent(new Event("userLogout"));
    navigate("/");
    closeMobileMenu();
  };

  const getInitials = (name) => {
    if (!name) return "U";

    const parts = name.trim().split(" ");

    if (parts.length === 1) {
      return parts[0][0].toUpperCase();
    }

    return (
      parts[0][0] + parts[parts.length - 1][0]
    ).toUpperCase();
  };

  const goToDashboard = () => {
    const user = JSON.parse(localStorage.getItem("currentUser"));

    if (!user) return;

    if (user.role === "patient") {
      navigate("/patient");
    } else if (user.role === "doctor") {
      navigate("/doctor");
    } else if (user.role === "admin") {
      navigate("/admin/dashboard");
    } else {
      navigate("/");
    }

    closeMobileMenu();
  };

  return (
    <nav className="navbar">
      <div className="navbar__container">

        <Link
          to="/"
          className="navbar__brand"
          onClick={closeMobileMenu}
        >
          <img
            src="/Medicurex.jpg"
            alt="MediCurex Logo"
            className="navbar__logo-image"
          />

          <h1 className="navbar__logo-text">
            MediCurex
          </h1>
        </Link>

        <button
          className="navbar__mobile-toggle"
          onClick={toggleMobileMenu}
          aria-label="Toggle menu"
          aria-expanded={isMobileMenuOpen}
        >
          <span className="navbar__mobile-toggle-icon"></span>
          <span className="navbar__mobile-toggle-icon"></span>
          <span className="navbar__mobile-toggle-icon"></span>
        </button>

        <div
          className={`navbar__menu ${
            isMobileMenuOpen ? "navbar__menu--open" : ""
          }`}
        >
          <ul className="navbar__links">

            <li className="navbar__item">
              <Link
                to="/"
                className="navbar__link"
                onClick={closeMobileMenu}
              >
                Home
              </Link>
            </li>

            <li className="navbar__item">
              <Link
                to="/services"
                className="navbar__link"
                onClick={closeMobileMenu}
              >
                Services
              </Link>
            </li>

            <li className="navbar__item">
              <Link
                to="/medicine"
                className="navbar__link"
                onClick={closeMobileMenu}
              >
                Medicine
              </Link>
            </li>

            <li className="navbar__item">
              <Link
                to="/contact"
                className="navbar__link"
                onClick={closeMobileMenu}
              >
                Contact
              </Link>
            </li>

            <li className="navbar__item">
              <button
                className="navbar__theme-button"
                onClick={toggleTheme}
                aria-label={
                  darkMode
                    ? "Switch to light mode"
                    : "Switch to dark mode"
                }
                title={
                  darkMode
                    ? "Light Mode"
                    : "Dark Mode"
                }
              >
                {darkMode ? (
                  <svg
                    width="20"
                    height="20"
                    viewBox="0 0 24 24"
                    fill="none"
                    xmlns="http://www.w3.org/2000/svg"
                  >
                    <circle
                      cx="12"
                      cy="12"
                      r="4"
                      stroke="currentColor"
                      strokeWidth="2"
                    />
                    <path
                      d="M12 2V4"
                      stroke="currentColor"
                      strokeWidth="2"
                      strokeLinecap="round"
                    />
                    <path
                      d="M12 20V22"
                      stroke="currentColor"
                      strokeWidth="2"
                      strokeLinecap="round"
                    />
                    <path
                      d="M4.93 4.93L6.34 6.34"
                      stroke="currentColor"
                      strokeWidth="2"
                      strokeLinecap="round"
                    />
                    <path
                      d="M17.66 17.66L19.07 19.07"
                      stroke="currentColor"
                      strokeWidth="2"
                      strokeLinecap="round"
                    />
                    <path
                      d="M2 12H4"
                      stroke="currentColor"
                      strokeWidth="2"
                      strokeLinecap="round"
                    />
                    <path
                      d="M20 12H22"
                      stroke="currentColor"
                      strokeWidth="2"
                      strokeLinecap="round"
                    />
                    <path
                      d="M4.93 19.07L6.34 17.66"
                      stroke="currentColor"
                      strokeWidth="2"
                      strokeLinecap="round"
                    />
                    <path
                      d="M17.66 6.34L19.07 4.93"
                      stroke="currentColor"
                      strokeWidth="2"
                      strokeLinecap="round"
                    />
                  </svg>
                ) : (
                  <svg
                    width="20"
                    height="20"
                    viewBox="0 0 24 24"
                    fill="none"
                    xmlns="http://www.w3.org/2000/svg"
                  >
                    <path
                      d="M21 12.79A9 9 0 1 1 11.21 3C11.21 3 11.21 3 11.21 3A7 7 0 0 0 21 12.79Z"
                      stroke="currentColor"
                      strokeWidth="2"
                      strokeLinecap="round"
                      strokeLinejoin="round"
                    />
                  </svg>
                )}
              </button>
            </li>

            {currentUser ? (
              <>
                <li className="navbar__item navbar__item--user">
                  <div className="navbar__user-info">
                    <div className="navbar__user-initials">
                      {getInitials(
                        currentUser.name || currentUser.email
                      )}
                    </div>

                    <span
                      className="navbar__user-name"
                      onClick={goToDashboard}
                      style={{ cursor: "pointer" }}
                      title="Go to Dashboard"
                    >
                      {currentUser.name || currentUser.email}
                    </span>
                  </div>
                </li>

                <li className="navbar__item navbar__item--action">
                  <button
                    className="navbar__logout-button"
                    onClick={handleLogout}
                  >
                    Logout
                  </button>
                </li>
              </>
            ) : (
              <>
                <li className="navbar__item navbar__item--action">
                  <Link
                    to="/login"
                    className="navbar__link navbar__link--primary"
                    onClick={closeMobileMenu}
                  >
                    Login
                  </Link>
                </li>

                <li className="navbar__item navbar__item--action">
                  <Link
                    to="/register"
                    className="navbar__link navbar__link--secondary"
                    onClick={closeMobileMenu}
                  >
                    Register
                  </Link>
                </li>
              </>
            )}
          </ul>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;