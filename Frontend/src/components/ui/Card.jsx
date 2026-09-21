import React from "react";
import "./Card.css";

const Card = ({
    children,
    className = "",
    variant = "default",
    hoverable = true,
    onClick,
    ...props
}) => {
    const cardClasses = [
        "card",
        variant && `card--${variant}`,
        hoverable && "card--hoverable",
        onClick && "card--clickable",
        className
    ]
        .filter(Boolean)
        .join(" ");

    return (
        <div
            className={cardClasses}
            onClick={onClick}
            role={onClick ? "button" : undefined}
            tabIndex={onClick ? 0 : undefined}
            onKeyDown={
                onClick
                    ? (event) => {
                          if (
                              event.key === "Enter" ||
                              event.key === " "
                          ) {
                              event.preventDefault();
                              onClick(event);
                          }
                      }
                    : undefined
            }
            {...props}
        >
            {children}
        </div>
    );
};

export default Card;