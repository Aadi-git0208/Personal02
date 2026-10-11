import React, { useEffect, useState } from "react";
import { getApiErrorMessage, pageContent } from "../../api/client";
import {
    createMedicine,
    listMedicines,
    updateMedicineStatus,
    updateMedicineStock
} from "../../api/pharmacyApi";
import "./Admin.css";

const emptyForm = {
    name: "",
    genericName: "",
    manufacturer: "",
    description: "",
    dosageForm: "TABLET",
    strength: "",
    category: "",
    unitPrice: "",
    stockQuantity: "0",
    prescriptionRequired: false,
    imageUrl: ""
};

const ManageMedicines = () => {
    const [medicines, setMedicines] = useState([]);
    const [form, setForm] = useState(emptyForm);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState("");

    async function loadMedicines() {
        setLoading(true);
        setError("");
        try {
            const page = await listMedicines({ size: 50, sort: "name,asc" });
            setMedicines(pageContent(page));
        } catch (loadError) {
            setError(getApiErrorMessage(loadError));
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        loadMedicines();
    }, []);

    async function addMedicine() {
        setError("");
        if (!form.name || !form.strength || !form.category || form.unitPrice === "") {
            setError("Name, strength, category, and unit price are required.");
            return;
        }
        setSaving(true);
        try {
            await createMedicine({
                name: form.name.trim(),
                genericName: form.genericName.trim() || undefined,
                manufacturer: form.manufacturer.trim() || undefined,
                description: form.description.trim() || undefined,
                dosageForm: form.dosageForm,
                strength: form.strength.trim(),
                category: form.category.trim(),
                unitPrice: Number(form.unitPrice),
                stockQuantity: Number(form.stockQuantity || 0),
                prescriptionRequired: Boolean(form.prescriptionRequired),
                imageUrl: form.imageUrl.trim() || undefined
            });
            setForm(emptyForm);
            await loadMedicines();
        } catch (saveError) {
            setError(getApiErrorMessage(saveError));
        } finally {
            setSaving(false);
        }
    }

    async function changeStatus(id, status) {
        setError("");
        try {
            await updateMedicineStatus(id, status);
            await loadMedicines();
        } catch (statusError) {
            setError(getApiErrorMessage(statusError));
        }
    }

    async function changeStock(id, quantity) {
        setError("");
        try {
            await updateMedicineStock(id, Number(quantity));
            await loadMedicines();
        } catch (stockError) {
            setError(getApiErrorMessage(stockError));
        }
    }

    return (
        <div className="admin-page">
            <h1>Manage Medicines</h1>
            {error && <p role="alert">{error}</p>}
            <div className="admin-section">
                <h2>Add Medicine</h2>
                <input className="admin-input" placeholder="Medicine Name" value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} />
                <input className="admin-input" placeholder="Generic name" value={form.genericName} onChange={(event) => setForm({ ...form, genericName: event.target.value })} />
                <input className="admin-input" placeholder="Manufacturer" value={form.manufacturer} onChange={(event) => setForm({ ...form, manufacturer: event.target.value })} />
                <select className="admin-input" value={form.dosageForm} onChange={(event) => setForm({ ...form, dosageForm: event.target.value })}>
                    {["TABLET", "CAPSULE", "SYRUP", "CREAM", "INJECTION", "DROPS", "INHALER", "OTHER"].map((formValue) => (
                        <option key={formValue} value={formValue}>{formValue}</option>
                    ))}
                </select>
                <input className="admin-input" placeholder="Strength" value={form.strength} onChange={(event) => setForm({ ...form, strength: event.target.value })} />
                <input className="admin-input" placeholder="Category" value={form.category} onChange={(event) => setForm({ ...form, category: event.target.value })} />
                <input className="admin-input" type="number" placeholder="Unit price" value={form.unitPrice} onChange={(event) => setForm({ ...form, unitPrice: event.target.value })} />
                <input className="admin-input" type="number" placeholder="Stock quantity" value={form.stockQuantity} onChange={(event) => setForm({ ...form, stockQuantity: event.target.value })} />
                <input className="admin-input" placeholder="Image URL" value={form.imageUrl} onChange={(event) => setForm({ ...form, imageUrl: event.target.value })} />
                <label>
                    <input
                        type="checkbox"
                        checked={form.prescriptionRequired}
                        onChange={(event) => setForm({ ...form, prescriptionRequired: event.target.checked })}
                    />
                    Prescription required
                </label>
                <button className="admin-btn" onClick={addMedicine} disabled={saving}>
                    {saving ? "Saving..." : "Add Medicine"}
                </button>
            </div>
            <div className="admin-section">
                <h2>Medicine List</h2>
                {loading && <p>Loading catalog...</p>}
                {!loading && medicines.length === 0 && <p className="admin-empty">No medicines available</p>}
                {medicines.map((medicine) => (
                    <div className="admin-row" key={medicine.id}>
                        <span>
                            {medicine.name} – ₹{medicine.unitPrice} · {medicine.status} · stock {medicine.stockQuantity}
                            {medicine.prescriptionRequired ? " · Rx" : ""}
                        </span>
                        <span>
                            <button className="admin-btn" onClick={() => changeStatus(medicine.id, "ACTIVE")}>Activate</button>
                            <button className="admin-btn" onClick={() => changeStatus(medicine.id, "INACTIVE")}>Deactivate</button>
                            <button
                                className="admin-btn"
                                onClick={() => {
                                    const quantity = window.prompt("New stock quantity", String(medicine.stockQuantity));
                                    if (quantity != null) {
                                        changeStock(medicine.id, quantity);
                                    }
                                }}
                            >
                                Stock
                            </button>
                        </span>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default ManageMedicines;
