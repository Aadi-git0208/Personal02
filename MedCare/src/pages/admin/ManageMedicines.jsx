import React, { useState } from "react";
import "./Admin.css";

const ManageMedicines = () => {
  const [medicines, setMedicines] = useState(
    JSON.parse(localStorage.getItem("medicines")) || []
  );

  const [name, setName] = useState("");
  const [price, setPrice] = useState("");
  const [image, setImage] = useState(""); 

  const handleImageUpload = (e) => {
    const file = e.target.files[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onloadend = () => {
      setImage(reader.result); 
    };
    reader.readAsDataURL(file);
  };

  const addMedicine = () => {
    if (!name || !price) {
      alert("Please fill all fields");
      return;
    }

    const newMedicine = {
      id: Date.now(),
      name,
      price: Number(price),
      image, 
    };

    const updated = [...medicines, newMedicine];
    setMedicines(updated);
    localStorage.setItem("medicines", JSON.stringify(updated));

    setName("");
    setPrice("");
    setImage("");

    alert("Medicine added successfully ✅");
  };

  const deleteMedicine = (id) => {
    const updated = medicines.filter((m) => m.id !== id);
    setMedicines(updated);
    localStorage.setItem("medicines", JSON.stringify(updated));
  };

  return (
    <div className="admin-page">
      <h1>Manage Medicines</h1>

      {/* ===== ADD MEDICINE ===== */}
      <div className="admin-section">
        <h2>Add Medicine</h2>

        <input
          className="admin-input"
          type="text"
          placeholder="Medicine Name"
          value={name}
          onChange={(e) => setName(e.target.value)}
        />

        <input
          className="admin-input"
          type="number"
          placeholder="Price"
          value={price}
          onChange={(e) => setPrice(e.target.value)}
        />

        {/* IMAGE UPLOAD */}
        <input
          className="admin-input"
          type="file"
          accept="image/*"
          onChange={handleImageUpload}
        />

        {/* IMAGE PREVIEW */}
        {image && (
          <img
            src={image}
            alt="preview"
            style={{
              width: "120px",
              height: "80px",
              objectFit: "cover",
              borderRadius: "10px",
              marginBottom: "10px",
            }}
          />
        )}

        <button className="admin-btn" onClick={addMedicine}>
          Add Medicine
        </button>
      </div>

      {/* ===== MEDICINE LIST ===== */}
      <div className="admin-section">
        <h2>Medicine List</h2>

        {medicines.length === 0 ? (
          <p className="admin-empty">No medicines available</p>
        ) : (
          medicines.map((m) => (
            <div className="admin-row" key={m.id}>
              <span>
                {m.image && (
                  <img
                    src={m.image}
                    alt={m.name}
                    style={{
                      width: "50px",
                      height: "35px",
                      objectFit: "cover",
                      borderRadius: "6px",
                      marginRight: "10px",
                      verticalAlign: "middle",
                    }}
                  />
                )}
                {m.name} – ₹{m.price}
              </span>

              <button
                className="admin-btn admin-btn-danger"
                onClick={() => deleteMedicine(m.id)}
              >
                Delete
              </button>
            </div>
          ))
        )}
      </div>
    </div>
  );
};

export default ManageMedicines;
