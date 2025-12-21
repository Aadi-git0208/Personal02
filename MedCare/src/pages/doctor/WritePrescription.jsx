import React, { useState } from "react";

const WritePrescription = () => {
  const [medicine, setMedicine] = useState("");

  return (
    <div>
      <h2>Write Prescription</h2>

      <input
        type="text"
        placeholder="Medicine Name"
        value={medicine}
        onChange={(e) => setMedicine(e.target.value)}
      />

      <textarea placeholder="Dosage Instructions"></textarea>

      <button>Save Prescription</button>
    </div>
  );
};

export default WritePrescription;
