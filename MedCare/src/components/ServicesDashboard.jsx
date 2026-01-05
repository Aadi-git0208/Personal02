import React from "react";
import "./ServicesDashboard.css";
import ServiceCard from "./ui/ServiceCard";
import {
  FaUserMd,
  FaCalendarCheck,
  FaPills,
  FaVial,
  FaFileMedical,
  FaAmbulance,
} from "react-icons/fa";

const Services = () => {
  const services = [
    {
      icon: <FaUserMd />,
      title: "Online Consultation",
      description: "Consult certified doctors anytime via video or chat.",
      iconColor: "#0ea5e9"
    },
    {
      icon: <FaCalendarCheck />,
      title: "Appointment Booking",
      description: "Book hospital and clinic appointments easily.",
      iconColor: "#10b981"
    },
    {
      icon: <FaPills />,
      title: "Medicine Delivery",
      description: "Order medicines online with doorstep delivery.",
      iconColor: "#3b82f6"
    },
    {
      icon: <FaVial />,
      title: "Diagnostic Tests",
      description: "Book lab tests and access reports digitally.",
      iconColor: "#8b5cf6"
    },
    {
      icon: <FaFileMedical />,
      title: "Health Records",
      description: "Securely store prescriptions and medical history.",
      iconColor: "#06b6d4"
    },
    {
      icon: <FaAmbulance />,
      title: "Emergency SOS",
      description: "One-click emergency assistance and ambulance help.",
      iconColor: "#ef4444"
    }
  ];

  return (
    <section className="services-section">
      <div className="services-container">
        <h2 className="services-title">Our Services</h2>
        <div className="services-grid">
          {services.map((service, index) => (
            <ServiceCard
              key={index}
              icon={service.icon}
              title={service.title}
              description={service.description}
              iconColor={service.iconColor}
            />
          ))}
        </div>
      </div>
    </section>
  );
};

export default Services;
