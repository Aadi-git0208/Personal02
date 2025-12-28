import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import MedicineCard from "./MedicineCard";
import "./MedicineDashboard.css";

const MedicineDashboard = () => {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem("currentUser"));

  /* ===== MEDICINES ===== */
  const [medicines, setMedicines] = useState(() => {
    const stored = JSON.parse(localStorage.getItem("medicines"));
    if (stored && stored.length > 0) return stored;
    return [];
  });

  // Refresh medicines when localStorage changes
  useEffect(() => {
    const loadMedicines = () => {
      try {
        const stored = JSON.parse(localStorage.getItem("medicines")) || [];
        setMedicines(stored);
      } catch (err) {
        console.error("Error loading medicines:", err);
      }
    };

    // Load initially
    loadMedicines();

    // Listen for storage events
    window.addEventListener("storage", loadMedicines);

    // Check periodically for updates
    const interval = setInterval(loadMedicines, 1000);

    return () => {
      window.removeEventListener("storage", loadMedicines);
      clearInterval(interval);
    };
  }, []);

  /* ===== CART ===== */
  const [cart, setCart] = useState(() => {
    return JSON.parse(localStorage.getItem("cart")) || [];
  });

  useEffect(() => {
    localStorage.setItem("cart", JSON.stringify(cart));
  }, [cart]);

  /* ===== ADD TO CART ===== */
  const handleAddToCart = (medicine, quantity = 1) => {
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
            ? { ...item, quantity: item.quantity + quantity }
            : item
        );
      }

      return [...prevCart, { ...medicine, quantity: quantity }];
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

      {medicines.length === 0 ? (
        <div style={{ textAlign: "center", padding: "60px 20px" }}>
          <p style={{ fontSize: "18px", opacity: 0.7, marginBottom: "20px" }}>
            No medicines available at the moment.
          </p>
          <p style={{ fontSize: "14px", opacity: 0.5 }}>
            Please check back later or contact the administrator.
          </p>
        </div>
      ) : (
        <div className="medicine-grid-vertical">
          {medicines.map((med) => (
            <MedicineCard
              key={med.id}
              medicine={med}
              onAddToCart={handleAddToCart}
              showAddToCart={true}
              userRole={user?.role || null}
            />
          ))}
        </div>
      )}

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
