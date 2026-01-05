import React from 'react';
import './Home.css';
import Services from '../components/ServicesDashboard';  

const Home = () => {
  return (
    <>
      <section className="hero-section">
        <div className="hero-video">
          <video autoPlay muted loop playsInline className="hero-video__element" preload="metadata">
            <source src="/Bg.mp4" type="video/mp4" />
          </video>
          <div className="hero-overlay">
            <div className="hero-content">
              <h1 className="hero-title">Welcome to MediCurex</h1>
              <p className="hero-description">
                MediCurex is a digital healthcare platform that connects patients and doctors on a single secure interface. It allows users to book appointments, consult doctors online, manage medical records, and access medicines easily, ensuring fast, reliable, and technology-driven healthcare services.
              </p>
            </div>
          </div>
        </div>
      </section>
      
      <section className="about-section">
        <div className="about-container">
          <h2 className="about-title">About Us</h2>
          <p className="about-description">
            At MediCurex, we are dedicated to revolutionizing healthcare by leveraging technology to provide seamless access to medical services. Our platform is designed to bridge the gap between patients and healthcare providers, ensuring that quality care is just a click away. Whether you need to consult with a specialist, manage your health records, or order medications, MediCurex is here to make healthcare more accessible and efficient for everyone.
          </p>
        </div>
      </section>
      
      <Services />
    
      <footer className="site-footer">
        <p className="footer-text">&copy; 2025 MediCurex. All rights reserved.</p>
      </footer>
    </>
  );
};

export default Home;
