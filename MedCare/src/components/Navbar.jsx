import React, { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import "./Navbar.css";

const Navbar = () => {

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
    if (parts.length === 1) return parts[0][0].toUpperCase();
    return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
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

        <Link to="/" className="navbar__brand" onClick={closeMobileMenu}>
          <img
            src="/Medicurex.jpg"
            alt="MediCurex Logo"
            className="navbar__logo-image"
          />
          <h1 className="navbar__logo-text">MediCurex</h1>
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

        <div className={`navbar__menu ${isMobileMenuOpen ? "navbar__menu--open" : ""}`}>
          <ul className="navbar__links">
            <li className="navbar__item">
              <Link to="/" className="navbar__link" onClick={closeMobileMenu}>Home</Link>
            </li>

            <li className="navbar__item">
              <Link to="/services" className="navbar__link" onClick={closeMobileMenu}>Services</Link>
            </li>

            <li className="navbar__item">
              <Link to="/medicine" className="navbar__link" onClick={closeMobileMenu}>Medicine</Link>
            </li>
            
            <li className="navbar__item">
              <Link to="/contact" className="navbar__link" onClick={closeMobileMenu}>Contact</Link>
            </li>

            {currentUser ? (
              <>
                <li className="navbar__item navbar__item--user">
                  <div className="navbar__user-info">
                    <div className="navbar__user-initials">
                      {getInitials(currentUser.name || currentUser.email)}
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
                  <button className="navbar__logout-button" onClick={handleLogout}>
                    Logout
                  </button>
                </li>
              </>
            ) : (
              <>
                <li className="navbar__item navbar__item--action">
                  <Link to="/login" className="navbar__link navbar__link--primary" onClick={closeMobileMenu}>
                    Login
                  </Link>
                </li>

                <li className="navbar__item navbar__item--action">
                  <Link to="/register" className="navbar__link navbar__link--secondary" onClick={closeMobileMenu}>
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
