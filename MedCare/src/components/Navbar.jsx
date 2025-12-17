import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import './Navbar.css';

const Navbar = () => {
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [currentUser, setCurrentUser] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    // Check if user is logged in
    const user = localStorage.getItem('currentUser');
    if (user) {
      try {
        setCurrentUser(JSON.parse(user));
      } catch (e) {
        setCurrentUser(null);
      }
    }
  }, []);

  // Listen for storage changes to update when user logs in/out
  useEffect(() => {
    const handleStorageChange = () => {
      const user = localStorage.getItem('currentUser');
      if (user) {
        try {
          setCurrentUser(JSON.parse(user));
        } catch (e) {
          setCurrentUser(null);
        }
      } else {
        setCurrentUser(null);
      }
    };

    window.addEventListener('storage', handleStorageChange);
    // Also listen for custom event for same-tab updates
    window.addEventListener('userLogin', handleStorageChange);
    window.addEventListener('userLogout', handleStorageChange);

    return () => {
      window.removeEventListener('storage', handleStorageChange);
      window.removeEventListener('userLogin', handleStorageChange);
      window.removeEventListener('userLogout', handleStorageChange);
    };
  }, []);

  const toggleMobileMenu = () => {
    setIsMobileMenuOpen(!isMobileMenuOpen);
  };

  const closeMobileMenu = () => {
    setIsMobileMenuOpen(false);
  };

  const handleLogout = () => {
    localStorage.removeItem('currentUser');
    localStorage.removeItem('isLoggedIn');
    setCurrentUser(null);
    window.dispatchEvent(new Event('userLogout'));
    navigate('/');
    closeMobileMenu();
  };

  const getInitials = (name) => {
    if (!name) return 'U';
    const names = name.trim().split(' ');
    if (names.length === 1) {
      return names[0].charAt(0).toUpperCase();
    }
    return (names[0].charAt(0) + names[names.length - 1].charAt(0)).toUpperCase();
  };

  return (
    <nav className="navbar">
      <div className="navbar__container">
        <div className="navbar__brand">
          <img
            src="/Medicurex.jpg"
            alt="MediCurex Logo"
            className="navbar__logo-image"
          />
          <h1 className="navbar__logo-text">MediCurex</h1>
        </div>

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

        <div className={`navbar__menu ${isMobileMenuOpen ? 'navbar__menu--open' : ''}`}>
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
              <Link to="/doctor" className="navbar__link" onClick={closeMobileMenu}>Doctor</Link>
            </li>
            <li className="navbar__item">
              <Link to="/contact" className="navbar__link" onClick={closeMobileMenu}>Contact</Link>
            </li>
            {currentUser ? (
              <>
                <li className="navbar__item navbar__item--user">
                  <div className="navbar__user-info">
                    <div className="navbar__user-initials" title={currentUser.name || currentUser.email}>
                      {getInitials(currentUser.name || currentUser.email)}
                    </div>
                    <span className="navbar__user-name">{currentUser.name || currentUser.email}</span>
                  </div>
                </li>
                <li className="navbar__item navbar__item--action">
                  <button 
                    className="navbar__logout-button" 
                    onClick={handleLogout}
                    aria-label="Logout"
                  >
                    Logout
                  </button>
                </li>
              </>
            ) : (
              <>
                <li className="navbar__item navbar__item--action">
                  <Link to="/login" className="navbar__link navbar__link--primary" onClick={closeMobileMenu}>Login</Link>
                </li>
                <li className="navbar__item navbar__item--action">
                  <Link to="/register" className="navbar__link navbar__link--secondary" onClick={closeMobileMenu}>Register</Link>
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
