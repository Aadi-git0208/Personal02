import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import MedicineCard from "./MedicineCard";
import "./MedicineDashboard.css";

const MedicineDashboard = () => {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem("currentUser"));

  const [medicines, setMedicines] = useState(() => {
    const stored = JSON.parse(localStorage.getItem("medicines"));
    return stored && stored.length > 0 ? stored : [];
  });

  const [cart, setCart] = useState(() => {
    return JSON.parse(localStorage.getItem("cart")) || [];
  });

  const [search, setSearch] = useState("");

  useEffect(() => {
    const loadMedicines = () => {
      try {
        const stored = JSON.parse(localStorage.getItem("medicines")) || [];
        setMedicines(stored);
      } catch (err) {
        console.error(err);
      }
    };

    loadMedicines();
    window.addEventListener("storage", loadMedicines);
    const interval = setInterval(loadMedicines, 1000);

    return () => {
      window.removeEventListener("storage", loadMedicines);
      clearInterval(interval);
    };
  }, []);

  useEffect(() => {
    localStorage.setItem("cart", JSON.stringify(cart));
  }, [cart]);

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

    setCart(prev => {
      const exists = prev.find(item => item.id === medicine.id);
      if (exists) {
        return prev.map(item =>
          item.id === medicine.id
            ? { ...item, quantity: item.quantity + quantity }
            : item
        );
      }
      return [...prev, { ...medicine, quantity }];
    });
  };

  const handleBuyNow = (medicine) => {
    handleAddToCart(medicine, 1);
    navigate("/cart");
  };

  const filteredMedicines = medicines.filter(m =>
    m.name.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="medicine-page">
      <h1 className="medicine-title">Medicines</h1>

      <div className="medicine-top-bar">
        <input
          type="text"
          placeholder="Search medicines..."
          className="medicine-search"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />

        <div
          className="medicine-cart-btn"
          onClick={() => navigate("/cart")}
        >
          🛒 Cart
        </div>
      </div>

      <h2>Available Medicines</h2>

      {filteredMedicines.length === 0 ? (
        <div style={{ textAlign: "center", padding: "60px 20px" }}>
          <p style={{ fontSize: "18px", opacity: 0.7 }}>
            No medicines available
          </p>
        </div>
      ) : (
        <div className="medicine-grid-vertical">
          {filteredMedicines.map((med) => (
            <MedicineCard
              key={med.id}
              medicine={med}
              onAddToCart={handleAddToCart}
              onBuyNow={handleBuyNow}
              userRole={user?.role || null}
            />
          ))}
        </div>
      )}

    </div>
  );
};

export default MedicineDashboard;
