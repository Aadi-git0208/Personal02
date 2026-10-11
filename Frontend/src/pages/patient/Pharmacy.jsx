import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import MedicineCard from "../../components/MedicineCard";
import "../../components/MedicineDashboard.css";
import { getApiErrorMessage, pageContent } from "../../api/client";
import { addCartItem } from "../../api/cart";
import { listMedicineCategories, listMedicines } from "../../api/pharmacyApi";
import { useAuth } from "../../auth/AuthContext";

const Pharmacy = () => {
    const navigate = useNavigate();
    const { user } = useAuth();
    const [medicines, setMedicines] = useState([]);
    const [categories, setCategories] = useState([]);
    const [search, setSearch] = useState("");
    const [category, setCategory] = useState("");
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [notice, setNotice] = useState("");

    useEffect(() => {
        let cancelled = false;
        async function loadCategories() {
            try {
                const result = await listMedicineCategories();
                if (!cancelled) {
                    setCategories(result?.categories || []);
                }
            } catch {
                if (!cancelled) {
                    setCategories([]);
                }
            }
        }
        loadCategories();
        return () => {
            cancelled = true;
        };
    }, []);

    useEffect(() => {
        let cancelled = false;
        async function loadMedicines() {
            setLoading(true);
            setError("");
            try {
                const result = await listMedicines({
                    q: search.trim() || undefined,
                    category: category || undefined,
                    page,
                    size: 12,
                    sort: "name,asc"
                });
                if (!cancelled) {
                    setMedicines(pageContent(result));
                    setTotalPages(result?.totalPages || 0);
                }
            } catch (loadError) {
                if (!cancelled) {
                    setError(getApiErrorMessage(loadError));
                }
            } finally {
                if (!cancelled) {
                    setLoading(false);
                }
            }
        }
        const handle = setTimeout(loadMedicines, 250);
        return () => {
            cancelled = true;
            clearTimeout(handle);
        };
    }, [search, category, page]);

    const handleAddToCart = (medicine, quantity = 1) => {
        addCartItem(medicine, quantity);
        setNotice(`${medicine.name} added to cart.`);
    };

    return (
        <div className="medicine-page">
            <h1 className="medicine-title">Pharmacy</h1>
            <div className="medicine-top-bar">
                <input
                    type="text"
                    placeholder="Search medicines..."
                    className="medicine-search"
                    value={search}
                    onChange={(event) => {
                        setSearch(event.target.value);
                        setPage(0);
                    }}
                />
                <select
                    className="medicine-search"
                    value={category}
                    onChange={(event) => {
                        setCategory(event.target.value);
                        setPage(0);
                    }}
                >
                    <option value="">All categories</option>
                    {categories.map((item) => (
                        <option key={item} value={item}>{item}</option>
                    ))}
                </select>
                <button className="medicine-cart-btn" onClick={() => navigate("/cart")}>
                    Cart
                </button>
            </div>
            {notice && <p>{notice}</p>}
            {loading && <p>Loading medicines...</p>}
            {error && (
                <div className="pharmacy-message pharmacy-error">
                    <p>{error}</p>
                </div>
            )}
            <h2 className="pharmacy-heading">Available Medicines</h2>
            {!loading && medicines.length === 0 ? (
                <div className="pharmacy-empty">
                    <p className="pharmacy-empty-title">No medicines available at the moment.</p>
                    <p className="pharmacy-empty-subtitle">Please check back later or contact the administrator.</p>
                </div>
            ) : (
                <div className="medicine-grid-vertical">
                    {medicines.map((medicine) => (
                        <MedicineCard
                            key={medicine.id}
                            medicine={medicine}
                            onAddToCart={handleAddToCart}
                            onBuyNow={(item, quantity) => {
                                handleAddToCart(item, quantity);
                                navigate("/cart");
                            }}
                            showAddToCart={true}
                            userRole={user?.role || null}
                        />
                    ))}
                </div>
            )}
            {totalPages > 1 && (
                <div>
                    <button type="button" disabled={page <= 0} onClick={() => setPage((current) => current - 1)}>Previous</button>
                    <span> Page {page + 1} of {totalPages} </span>
                    <button type="button" disabled={page + 1 >= totalPages} onClick={() => setPage((current) => current + 1)}>Next</button>
                </div>
            )}
        </div>
    );
};

export default Pharmacy;
