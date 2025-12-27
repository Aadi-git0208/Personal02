import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "./MedicineDashboard.css";

const DEFAULT_MEDICINES = [
  { id: 1, name: "Paracetamol", price: 50, image: "/Paracetamol.webp" },
  { id: 2, name: "Amoxicillin", price: 120, image: "" },
  { id: 3, name: "Cetirizine", price: 30, image: "" },
];

const MedicineDashboard = () => {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem("currentUser"));

  /* ===== MEDICINES ===== */
  const [medicines] = useState(() => {
  const stored = JSON.parse(localStorage.getItem("medicines"));
  if (stored && stored.length > 0) return stored;

  localStorage.setItem("medicines", JSON.stringify(DEFAULT_MEDICINES));
  return DEFAULT_MEDICINES;
});

  /* ===== CART ===== */
  const [cart, setCart] = useState(() => {
    return JSON.parse(localStorage.getItem("cart")) || [];
  });

  useEffect(() => {
    localStorage.setItem("cart", JSON.stringify(cart));
  }, [cart]);

  /* ===== ADD TO CART ===== */
  const handleAddToCart = (medicine) => {
    if (!user) {
      alert("Please login to buy medicines");
      navigate("/login");
      return;
    }

    if (user.role !== "patient") {
      alert("Only patients can buy medicines");
      return;
    }

    setCart((prevCart) => {
      const exists = prevCart.find((item) => item.id === medicine.id);

      if (exists) {
        return prevCart.map((item) =>
          item.id === medicine.id
            ? { ...item, quantity: item.quantity + 1 }
            : item
        );
      }

      return [...prevCart, { ...medicine, quantity: 1 }];
    });
  };

  /* ===== TOTAL ===== */
  const totalAmount = cart.reduce(
    (sum, item) => sum + item.price * item.quantity,
    0
  );

  return (
    <div className="medicine-page">
      <h1 className="medicine-title">Medicines</h1>

      {/* ===== AVAILABLE MEDICINES ONLY ===== */}
      <h2>Available Medicines</h2>

      <div className="medicine-grid">
        {medicines.map((med) => (
          <div className="medicine-card" key={med.id}>
            <div className="medicine-image">
              {med.image ? (
                <img src={med.image} alt={med.name} />
              ) : (
                <span>No Image</span>
              )}
            </div>

            <h4 className="medicine-name">{med.name}</h4>
            <p className="medicine-price">₹{med.price}</p>

            {user?.role === "patient" && (
              <button
                className="add-cart-btn"
                onClick={() => handleAddToCart(med)}
              >
                Add to Cart
              </button>
            )}
          </div>
        ))}
      </div>

      {/* ===== CART (PATIENT ONLY) ===== */}
      {user?.role === "patient" && (
        <div className="cart-section">
          <h2>Your Cart</h2>

          {cart.length === 0 ? (
            <p>Cart is empty</p>
          ) : (
            <>
              {cart.map((item) => (
                <div className="cart-item" key={item.id}>
                  {item.name} × {item.quantity} = ₹
                  {item.price * item.quantity}
                </div>
              ))}
              <div className="cart-total">
                Total: ₹{totalAmount}
              </div>
            </>
          )}
        </div>
      )}
    </div>
  );
};

export default MedicineDashboard;
