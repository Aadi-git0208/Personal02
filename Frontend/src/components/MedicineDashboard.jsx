import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import MedicineCard from "./MedicineCard";
import ScrollReveal from "./ui/ScrollReveal";
import "./MedicineDashboard.css";

const MedicineDashboard = () => {
    const navigate = useNavigate();

    const user =
        JSON.parse(localStorage.getItem("currentUser")) ||
        null;

    const [medicines, setMedicines] = useState(() => {
        try {
            const stored =
                JSON.parse(
                    localStorage.getItem("medicines")
                ) || [];

            return stored.length > 0 ? stored : [];
        } catch {
            return [];
        }
    });

    const [cart, setCart] = useState(() => {
        try {
            return (
                JSON.parse(
                    localStorage.getItem("cart")
                ) || []
            );
        } catch {
            return [];
        }
    });

    const [search, setSearch] = useState("");

    useEffect(() => {
        const loadMedicines = () => {
            try {
                const stored =
                    JSON.parse(
                        localStorage.getItem("medicines")
                    ) || [];

                setMedicines(stored);
            } catch (error) {
                console.error(
                    "Error loading medicines:",
                    error
                );
            }
        };

        loadMedicines();

        window.addEventListener(
            "storage",
            loadMedicines
        );

        const interval = setInterval(
            loadMedicines,
            1000
        );

        return () => {
            window.removeEventListener(
                "storage",
                loadMedicines
            );

            clearInterval(interval);
        };
    }, []);

    useEffect(() => {
        localStorage.setItem(
            "cart",
            JSON.stringify(cart)
        );
    }, [cart]);

    const handleAddToCart = (
        medicine,
        quantity = 1
    ) => {
        if (!user) {
            alert("Please login to buy medicines");
            navigate("/login");
            return;
        }

        if (user.role !== "patient") {
            alert("Only patients can buy medicines");
            return;
        }

        setCart((previousCart) => {
            const exists = previousCart.find(
                (item) => item.id === medicine.id
            );

            if (exists) {
                return previousCart.map((item) =>
                    item.id === medicine.id
                        ? {
                              ...item,
                              quantity:
                                  item.quantity +
                                  quantity
                          }
                        : item
                );
            }

            return [
                ...previousCart,
                {
                    ...medicine,
                    quantity: quantity
                }
            ];
        });
    };

    const handleBuyNow = (medicine) => {
        if (!user) {
            alert("Please login to buy medicines");
            navigate("/login");
            return;
        }

        if (user.role !== "patient") {
            alert("Only patients can buy medicines");
            return;
        }

        handleAddToCart(medicine, 1);
        navigate("/cart");
    };

    const filteredMedicines = medicines.filter(
        (medicine) =>
            medicine.name
                ?.toLowerCase()
                .includes(search.toLowerCase())
    );

    return (
        <div className="medicine-page">
            <h1 className="medicine-title">
                Medicines
            </h1>

            <div className="medicine-top-bar">
                <input
                    type="text"
                    placeholder="Search medicines..."
                    className="medicine-search"
                    value={search}
                    onChange={(event) =>
                        setSearch(event.target.value)
                    }
                />

                <button
                    className="medicine-cart-btn"
                    onClick={() =>
                        navigate("/cart")
                    }
                >
                    Cart
                </button>
            </div>

            <h2>Available Medicines</h2>

            {filteredMedicines.length === 0 ? (
                <div className="medicine-empty">
                    <p>
                        No medicines available
                    </p>
                </div>
            ) : (
                <ScrollReveal className="medicine-grid-vertical">
                    {filteredMedicines.map(
                        (medicine) => (
                            <MedicineCard
                                key={medicine.id}
                                medicine={medicine}
                                onAddToCart={
                                    handleAddToCart
                                }
                                onBuyNow={
                                    handleBuyNow
                                }
                                userRole={
                                    user?.role || null
                                }
                            />
                        )
                    )}
                </ScrollReveal>
            )}
        </div>
    );
};

export default MedicineDashboard;