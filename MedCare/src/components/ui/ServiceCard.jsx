import React from 'react';
import Card from './Card';
import './ServiceCard.css';

/**
 * ServiceCard Component
 * Specialized card for displaying service items with icon, title, and description
 */
const ServiceCard = ({ icon, title, description, iconColor, className = '' }) => {
  return (
    <Card className={`service-card ${className}`} hoverable>
      <div className="service-card__icon-wrapper">
        <div 
          className="service-card__icon" 
          style={iconColor ? { color: iconColor } : undefined}
        >
          {icon}
        </div>
      </div>
      <h3 className="service-card__title">{title}</h3>
      <p className="service-card__description">{description}</p>
    </Card>
  );
};

export default ServiceCard;

