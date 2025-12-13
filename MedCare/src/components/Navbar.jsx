import React from 'react';
import './Navbar.css';
const Navbar = () =>{
    return(
        <nav>
            <div className="navbar-container">
                <img src="/Medicurex.jpg" alt="MediCurex Logo" className="navbar-image"/>
                <h1 className="navbar-logo">MediCurex</h1>
                <ul className="navbar-links">
                    <li> 
                        <a href="#">Home</a>
                    </li>
                    <li>
                        <a href="#">Services</a>
                    </li>
                    <li>
                        <a href="#">Medicine</a>
                    </li>
                    <li>
                        <a href ="#">Doctor</a>
                    </li>
                    <li>
                        <a href="#">Contact</a>
                    </li>
                    <li>
                        <a href="#">Login</a>
                    </li>
                    <li>
                        <a href="#">Register</a>
                    </li>
                </ul>
            </div>
        </nav>

);

};
export default Navbar;
