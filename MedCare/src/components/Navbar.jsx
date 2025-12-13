import React from 'react';
import { Link } from 'react-router-dom';
import './Navbar.css';

const Navbar = () => {
  return (
    <nav>
      <div className="navbar-container">
        <img
          src="/Medicurex.jpg"
          alt="MediCurex Logo"
          className="navbar-image"
        />
        <h1 className="navbar-logo">MediCurex</h1>
      </div>

      <div className="Right-navbar">
        <ul className="navbar-links">
          <li>
            <Link to="/">Home</Link>
          </li>

          <li>
            <Link to="/services">Services</Link>
          </li>

          <li>
            <Link to="/medicine">Medicine</Link>
          </li>

          <li>
            <Link to="/doctor">Doctor</Link>
          </li>

          <li>
            <Link to="/contact">Contact</Link>
          </li>

          <li>
            <Link to="/login" className="btn-login">Login</Link>
          </li>

          <li>
            <Link to="/register">Register</Link>
          </li>
        </ul>
      </div>
    </nav>
  );
};

export default Navbar;
