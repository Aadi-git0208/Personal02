import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import './Navbar.css';

const Navbar = () => {
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

  const toggleMobileMenu = () => {
    setIsMobileMenuOpen(!isMobileMenuOpen);
  };

  const closeMobileMenu = () => {
    setIsMobileMenuOpen(false);
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
            <li className="navbar__item navbar__item--action">
              <Link to="/login" className="navbar__link navbar__link--primary" onClick={closeMobileMenu}>Login</Link>
            </li>
            <li className="navbar__item navbar__item--action">
              <Link to="/register" className="navbar__link navbar__link--secondary" onClick={closeMobileMenu}>Register</Link>
            </li>
          </ul>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
