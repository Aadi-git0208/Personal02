import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import MedicineCard from "../../components/MedicineCard";
import "../../components/MedicineDashboard.css";

const Pharmacy = () => {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem("currentUser")) || null;

  // State for medicines
  const [medicines, setMedicines] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Load medicines from localStorage and listen for changes
  useEffect(() => {
    const loadMedicines = () => {
      try {
        const storedMedicines = JSON.parse(localStorage.getItem("medicines")) || [];
        setMedicines(storedMedicines);
        setLoading(false);
        setError(null);
      } catch (err) {
        console.error("Error loading medicines:", err);
        setError("Failed to load medicines. Please try again later.");
        setLoading(false);
      }
    };

    // Load initially
    loadMedicines();

    // Listen for storage events (when localStorage is updated in another tab/window)
    window.addEventListener("storage", loadMedicines);

    // Also check periodically for updates (in case same tab updates)
    const interval = setInterval(loadMedicines, 1000);

    return () => {
      window.removeEventListener("storage", loadMedicines);
      clearInterval(interval);
    };
  }, []);

  // Add to cart handler
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

    try {
      const cart = JSON.parse(localStorage.getItem("cart")) || [];
      const exists = cart.find((item) => item.id === medicine.id);

      let updatedCart;
      if (exists) {
        updatedCart = cart.map((item) =>
          item.id === medicine.id
            ? { ...item, quantity: item.quantity + quantity }
            : item
        );
      } else {
        updatedCart = [...cart, { ...medicine, quantity: quantity }];
      }

      localStorage.setItem("cart", JSON.stringify(updatedCart));
      
      // Show feedback
      alert(`${medicine.name} (${quantity}x) added to cart!`);
    } catch (err) {
      console.error("Error adding to cart:", err);
      alert("Failed to add medicine to cart. Please try again.");
    }
  };

  // Loading state
  if (loading) {
    return (
      <div className="medicine-page">
        <h1 className="medicine-title">Pharmacy</h1>
        <div style={{ textAlign: "center", padding: "40px" }}>
          <p>Loading medicines...</p>
        </div>
      </div>
    );
  }

  // Error state
  if (error) {
    return (
      <div className="medicine-page">
        <h1 className="medicine-title">Pharmacy</h1>
        <div style={{ textAlign: "center", padding: "40px" }}>
          <p style={{ color: "#ef4444" }}>{error}</p>
          <button
            className="add-cart-btn-vertical"
            onClick={() => window.location.reload()}
            style={{ marginTop: "20px" }}
          >
            Retry
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="medicine-page">
      <h1 className="medicine-title">Pharmacy</h1>

      {/* Available Medicines Section */}
      <h2>Available Medicines</h2>

      {/* Empty state */}
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
          {medicines.map((medicine) => (
            <MedicineCard
              key={medicine.id}
              medicine={medicine}
              onAddToCart={handleAddToCart}
              showAddToCart={true}
              userRole={user?.role || null}
            />
          ))}
        </div>
      )}
    </div>
  );
};

export default Pharmacy;
