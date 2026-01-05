import React from "react";
import { useNavigate } from "react-router-dom";
import "./ServiceSection.css";

const Services = () => {
  const navigate = useNavigate();

  return (
    <div className="services-container">
      <h2>Our Services</h2>

      <div className="services-grid">
        <div
          className="service-card"
          onClick={() => navigate("/nearby-hospitals")}
        >
          🗺️
          <h3>Nearby Hospitals</h3>
          <p>Find hospitals near you in real time</p>
        </div>

        <div className="service-card">
          💊
          <h3>Buy Medicines</h3>
          <p>Order medicines online</p>
        </div>

        <div className="service-card">
          📅
          <h3>Book Appointment</h3>
          <p>Schedule doctor visits</p>
        </div>
      </div>
    </div>
  );
};

export default Services;
