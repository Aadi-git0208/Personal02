import React from 'react';
import './Card.css';

/**
 * Unified Card Component
 * A professional, reusable card component for displaying content consistently
 * across the application (services, doctors, medicines, etc.)
 */
const Card = ({ 
  children, 
  className = '', 
  variant = 'default',
  hoverable = true,
  onClick,
  ...props 
}) => {
  const cardClasses = [
    'card',
    variant && `card--${variant}`,
    hoverable && 'card--hoverable',
    onClick && 'card--clickable',
    className
  ].filter(Boolean).join(' ');

  return (
    <div 
      className={cardClasses}
      onClick={onClick}
      role={onClick ? 'button' : undefined}
      tabIndex={onClick ? 0 : undefined}
      onKeyDown={onClick ? (e) => {
        if (e.key === 'Enter' || e.key === ' ') {
          e.preventDefault();
          onClick(e);
        }
      } : undefined}
      {...props}
    >
      {children}
    </div>
  );
};

export default Card;

