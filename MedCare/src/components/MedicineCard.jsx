import React, { useState } from "react";
import "./MedicineDashboard.css";

/**
 * MedicineCard Component
 * Vertical e-commerce style card for displaying medicine information
 */
const MedicineCard = ({
  medicine,
  onAddToCart,
  showAddToCart = true,
  userRole = null,
}) => {
  const {
    id,
    name,
    price,
    image,
    description,
    composition,
    stock,
    availability,
    rating,
  } = medicine;

  const [quantity, setQuantity] = useState(1);
  const [imageError, setImageError] = useState(false);

  // Determine stock status
  const stockStatus = stock !== undefined ? stock : availability;
  const isInStock = stockStatus !== undefined 
    ? (typeof stockStatus === 'number' ? stockStatus > 0 : stockStatus === true || stockStatus === 'available')
    : true; // Default to available if not specified

  // Handle missing image gracefully
  const handleImageError = () => {
    setImageError(true);
  };

  // Handle quantity change
  const handleQuantityChange = (e) => {
    const newQuantity = parseInt(e.target.value) || 1;
    if (newQuantity > 0 && newQuantity <= 10) {
      setQuantity(newQuantity);
    }
  };

  // Handle add to cart
  const handleAddToCartClick = () => {
    if (onAddToCart && isInStock) {
      onAddToCart(medicine, quantity);
      // Reset quantity after adding
      setQuantity(1);
    }
  };

  return (
    <div className="medicine-card-vertical">
      {/* Medicine Image */}
      <div className="medicine-card-image">
        {image && !imageError ? (
          <img
            src={image}
            alt={name || "Medicine"}
            onError={handleImageError}
          />
        ) : (
          <div className="medicine-image-placeholder">
            <span>No Image</span>
          </div>
        )}
      </div>

      {/* Medicine Name */}
      <h4 className="medicine-card-name">{name || "Unnamed Medicine"}</h4>

      {/* Rating (if available) */}
      {rating !== undefined && rating !== null && (
        <div className="medicine-card-rating">
          <span className="rating-stars">
            {"★".repeat(Math.floor(rating))}
            {"☆".repeat(5 - Math.floor(rating))}
          </span>
          <span className="rating-value">({rating.toFixed(1)})</span>
        </div>
      )}

      {/* Price */}
      <div className="medicine-card-price">
        {price !== undefined && price !== null ? (
          <>₹{price}</>
        ) : (
          <span className="price-unavailable">Price not available</span>
        )}
      </div>

      {/* Description or Composition (if available) */}
      {(description || composition) && (
        <p className="medicine-card-description">
          {description || composition}
        </p>
      )}

      {/* Stock / Availability Status */}
      <div className="medicine-card-stock">
        {isInStock ? (
          <span className="stock-available">✓ In Stock</span>
        ) : (
          <span className="stock-unavailable">✗ Out of Stock</span>
        )}
        {typeof stockStatus === 'number' && (
          <span className="stock-count"> ({stockStatus} left)</span>
        )}
      </div>

      {/* Quantity Selector and Add to Cart Button */}
      {showAddToCart && userRole === "patient" && (
        <div className="medicine-card-actions">
          {isInStock ? (
            <>
              <div className="quantity-selector">
                <label htmlFor={`quantity-${id}`}>Qty:</label>
                <select
                  id={`quantity-${id}`}
                  value={quantity}
                  onChange={handleQuantityChange}
                  className="quantity-select"
                >
                  {[...Array(10)].map((_, i) => (
                    <option key={i + 1} value={i + 1}>
                      {i + 1}
                    </option>
                  ))}
                </select>
              </div>
              <button
                className="add-cart-btn-vertical"
                onClick={handleAddToCartClick}
                disabled={!isInStock}
              >
                Add to Cart
              </button>
            </>
          ) : (
            <button
              className="add-cart-btn-vertical"
              disabled
            >
              Out of Stock
            </button>
          )}
        </div>
      )}
    </div>
  );
};

export default MedicineCard;
