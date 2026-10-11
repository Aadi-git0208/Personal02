import React, { useState } from "react";
import "./MedicineDashboard.css";

const MedicineCard = ({
    medicine,
    onAddToCart,
    onBuyNow,
    showAddToCart = true,
    userRole = null
}) => {
    const {
        id,
        name,
        image,
        imageUrl,
        description,
        composition,
        genericName,
        strength,
        rating
    } = medicine || {};

    const price = medicine?.unitPrice ?? medicine?.price;
    const stock = medicine?.stockQuantity ?? medicine?.stock;
    const status = medicine?.status;
    const prescriptionRequired = Boolean(medicine?.prescriptionRequired);
    const displayImage = imageUrl || image;

    const [quantity, setQuantity] = useState(1);
    const [imageError, setImageError] = useState(false);

    const isInStock =
        status === "OUT_OF_STOCK" || status === "INACTIVE"
            ? false
            : typeof stock === "number"
                ? stock > 0
                : true;

    const handleQuantityChange = (event) => {
        const selectedQuantity = parseInt(event.target.value, 10) || 1;
        if (selectedQuantity >= 1 && selectedQuantity <= 10) {
            setQuantity(selectedQuantity);
        }
    };

    const handleAddToCartClick = () => {
        if (onAddToCart && isInStock) {
            onAddToCart(medicine, quantity);
            setQuantity(1);
        }
    };

    const handleBuyNowClick = () => {
        if (onBuyNow && isInStock) {
            onBuyNow(medicine, quantity);
            setQuantity(1);
        }
    };

    const ratingValue =
        typeof rating === "number" ? Math.max(0, Math.min(5, rating)) : null;
    const fullStars = ratingValue !== null ? Math.floor(ratingValue) : 0;
    const emptyStars = ratingValue !== null ? 5 - fullStars : 0;

    return (
        <div className="medicine-card-vertical">
            <div className="medicine-card-image">
                {displayImage && !imageError ? (
                    <img
                        src={displayImage}
                        alt={name || "Medicine"}
                        onError={() => setImageError(true)}
                    />
                ) : (
                    <div className="medicine-image-placeholder">No Image</div>
                )}
            </div>

            <h4 className="medicine-card-name">{name || "Unnamed Medicine"}</h4>
            {(genericName || strength) && (
                <p className="medicine-card-description">
                    {[genericName, strength].filter(Boolean).join(" · ")}
                </p>
            )}

            {ratingValue !== null && (
                <div className="medicine-card-rating">
                    <span className="rating-stars">
                        {"★".repeat(fullStars)}
                        {"☆".repeat(emptyStars)}
                    </span>
                    <span className="rating-value">({ratingValue.toFixed(1)})</span>
                </div>
            )}

            <div className="medicine-card-price">
                {price !== undefined ? `₹${price}` : "Price not available"}
            </div>

            {(description || composition) && (
                <p className="medicine-card-description">{description || composition}</p>
            )}

            <div className="medicine-card-stock">
                {isInStock ? (
                    <span className="stock-available">✓ In Stock</span>
                ) : (
                    <span className="stock-unavailable">✗ Out of Stock</span>
                )}
                {typeof stock === "number" && (
                    <span className="stock-count"> ({stock} left)</span>
                )}
                {prescriptionRequired && (
                    <span className="stock-unavailable"> Prescription required</span>
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
                                    {[...Array(10)].map((_, index) => (
                                        <option key={index + 1} value={index + 1}>
                                            {index + 1}
                                        </option>
                                    ))}
                                </select>
                            </div>
                            <div className="medicine-card-actions-row">
                                <button className="add-cart-btn-vertical" onClick={handleAddToCartClick}>
                                    Add to Cart
                                </button>
                                <button className="buy-now-btn" onClick={handleBuyNowClick}>
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
        </div>
    );
};

export default MedicineCard;
