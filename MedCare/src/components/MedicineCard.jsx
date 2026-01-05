import React, { useState } from "react";
import "./MedicineDashboard.css";

const MedicineCard = ({
  medicine,
  onAddToCart,
  onBuyNow,
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

  const stockStatus = stock !== undefined ? stock : availability;
  const isInStock =
    stockStatus !== undefined
      ? typeof stockStatus === "number"
        ? stockStatus > 0
        : stockStatus === true || stockStatus === "available"
      : true;

  const handleImageError = () => {
    setImageError(true);
  };

  const handleQuantityChange = (e) => {
    const q = parseInt(e.target.value) || 1;
    if (q > 0 && q <= 10) setQuantity(q);
  };

  const handleAddToCartClick = () => {
    if (onAddToCart && isInStock) {
      onAddToCart(medicine, quantity);
      setQuantity(1);
    }
  };

  const handleBuyNowClick = () => {
    if (onAddToCart && onBuyNow && isInStock) {
      onAddToCart(medicine, quantity);
      onBuyNow(medicine);
    }
  };

  return (
    <div className="medicine-card-vertical">
      <div className="medicine-card-image">
        {image && !imageError ? (
          <img src={image} alt={name} onError={handleImageError} />
        ) : (
          <div className="medicine-image-placeholder">No Image</div>
        )}
      </div>

      <h4 className="medicine-card-name">{name || "Unnamed Medicine"}</h4>

      {rating !== undefined && (
        <div className="medicine-card-rating">
          <span className="rating-stars">
            {"★".repeat(Math.floor(rating))}
            {"☆".repeat(5 - Math.floor(rating))}
          </span>
          <span className="rating-value">({rating.toFixed(1)})</span>
        </div>
      )}

      <div className="medicine-card-price">
        {price !== undefined ? `₹${price}` : "Price not available"}
      </div>

      {(description || composition) && (
        <p className="medicine-card-description">
          {description || composition}
        </p>
      )}

      <div className="medicine-card-stock">
        {isInStock ? (
          <span className="stock-available">✓ In Stock</span>
        ) : (
          <span className="stock-unavailable">✗ Out of Stock</span>
        )}
        {typeof stockStatus === "number" && (
          <span className="stock-count"> ({stockStatus} left)</span>
        )}
      </div>

      {showAddToCart && userRole === "patient" && (
        <div className="medicine-card-actions">
          {isInStock ? (
            <>
              <div className="quantity-selector">
                <label htmlFor={`qty-${id}`}>Qty:</label>
                <select
                  id={`qty-${id}`}
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

              <div className="medicine-card-actions-row">
                <button
                  className="add-cart-btn-vertical"
                  onClick={handleAddToCartClick}
                >
                  Add to Cart
                </button>

                <button
                  className="buy-now-btn"
                  onClick={handleBuyNowClick}
                >
                  Buy Now
                </button>
              </div>
            </>
          ) : (
            <button className="add-cart-btn-vertical" disabled>
              Out of Stock
            </button>
          )}
        </div>
      )}

      <div className="medicine-card-bottom-actions">
        <button className="feedback-btn">Feedback</button>
        <button className="message-btn">Message</button>
      </div>
    </div>
  );
};

export default MedicineCard;
