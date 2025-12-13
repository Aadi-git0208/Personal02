import React from "react";
import "./Services.css";
import {
  FaUserMd,
  FaCalendarCheck,
  FaPills,
  FaVial,
  FaFileMedical,
  FaAmbulance,
} from "react-icons/fa";

const Services = () => {
  return (
    <section className="services-section">
        <div className="services-box">
      <h2>Our Services</h2>
      <div className="services-grid">
        <div className="service-card">
          <div className="icon-circle blue">
            <FaUserMd />
          </div>
          <h3>Online Consultation</h3>
          <p>Consult certified doctors anytime via video or chat.</p>
        </div>

        <div className="service-card">
          <div className="icon-circle orange">
            <FaCalendarCheck />
          </div>
          <h3>Appointment Booking</h3>
          <p>Book hospital and clinic appointments easily.</p>
        </div>

        <div className="service-card">
          <div className="icon-circle purple">
            <FaPills />
          </div>
          <h3>Medicine Delivery</h3>
          <p>Order medicines online with doorstep delivery.</p>
        </div>

        <div className="service-card">
          <div className="icon-circle green">
            <FaVial />
          </div>
          <h3>Diagnostic Tests</h3>
          <p>Book lab tests and access reports digitally.</p>
        </div>

        <div className="service-card">
          <div className="icon-circle teal">
            <FaFileMedical />
          </div>
          <h3>Health Records</h3>
          <p>Securely store prescriptions and medical history.</p>
        </div>

        <div className="service-card">
          <div className="icon-circle red">
            <FaAmbulance />
          </div>
          <h3>Emergency SOS</h3>
          <p>One-click emergency assistance and ambulance help.</p>
        </div>
      </div>
        </div>
    </section>
  );
};

export default Services;
