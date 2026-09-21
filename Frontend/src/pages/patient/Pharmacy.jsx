import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import MedicineCard from "../../components/MedicineCard";
import "../../components/MedicineDashboard.css";

const Pharmacy = () => {
    const navigate = useNavigate();

    const user =
        JSON.parse(localStorage.getItem("currentUser")) ||
        null;

    const [medicines, setMedicines] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const loadMedicines = () => {
            try {
                const storedMedicines =
                    JSON.parse(
                        localStorage.getItem("medicines")
                    ) || [];

                setMedicines(storedMedicines);
                setLoading(false);
                setError(null);
            } catch (error) {
                console.error(
                    "Error loading medicines:",
                    error
                );

                setError(
                    "Failed to load medicines. Please try again later."
                );

                setLoading(false);
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

        try {
            const cart =
                JSON.parse(
                    localStorage.getItem("cart")
                ) || [];

            const exists = cart.find(
                (item) => item.id === medicine.id
            );

            let updatedCart;

            if (exists) {
                updatedCart = cart.map((item) =>
                    item.id === medicine.id
                        ? {
                              ...item,
                              quantity:
                                  item.quantity +
                                  quantity
                          }
                        : item
                );
            } else {
                updatedCart = [
                    ...cart,
                    {
                        ...medicine,
                        quantity: quantity
                    }
                ];
            }

            localStorage.setItem(
                "cart",
                JSON.stringify(updatedCart)
            );

            alert(
                `${medicine.name} (${quantity}x) added to cart!`
            );
        } catch (error) {
            console.error(
                "Error adding to cart:",
                error
            );

            alert(
                "Failed to add medicine to cart. Please try again."
            );
        }
    };

    if (loading) {
        return (
            <div className="medicine-page">
                <h1 className="medicine-title">
                    Pharmacy
                </h1>

                <div className="pharmacy-message">
                    <p>Loading medicines...</p>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="medicine-page">
                <h1 className="medicine-title">
                    Pharmacy
                </h1>

                <div className="pharmacy-message pharmacy-error">
                    <p>{error}</p>

                    <button
                        className="add-cart-btn-vertical pharmacy-retry-btn"
                        onClick={() =>
                            window.location.reload()
                        }
                    >
                        Retry
                    </button>
                </div>
            </div>
        );
    }

    return (
        <div className="medicine-page">
            <h1 className="medicine-title">
                Pharmacy
            </h1>

            <h2 className="pharmacy-heading">
                Available Medicines
            </h2>

            {medicines.length === 0 ? (
                <div className="pharmacy-empty">
                    <p className="pharmacy-empty-title">
                        No medicines available at the
                        moment.
                    </p>

                    <p className="pharmacy-empty-subtitle">
                        Please check back later or
                        contact the administrator.
                    </p>
                </div>
            ) : (
                <div className="medicine-grid-vertical">
                    {medicines.map((medicine) => (
                        <MedicineCard
                            key={medicine.id}
                            medicine={medicine}
                            onAddToCart={
                                handleAddToCart
                            }
                            showAddToCart={true}
                            userRole={
                                user?.role || null
                            }
                        />
                    ))}
                </div>
            )}
        </div>
    );
};

export default Pharmacy;