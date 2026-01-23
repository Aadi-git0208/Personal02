import { useState } from "react";

export default function DoctorPrescription() {
  const [patientName, setPatientName] = useState("");
  const [diagnosis, setDiagnosis] = useState("");
  const [medicines, setMedicines] = useState([
    { name: "", dose: "", duration: "", notes: "" }
  ]);

  const addRow = () => {
    setMedicines([...medicines, { name: "", dose: "", duration: "", notes: "" }]);
  };

  const handleChange = (i, field, value) => {
    const copy = [...medicines];
    copy[i][field] = value;
    setMedicines(copy);
  };

  const savePrescription = () => {
    const data = {
      patientName,
      diagnosis,
      medicines,
      doctor: "Dr. Aditya Shrivastava",
      date: new Date().toLocaleDateString()
    };

    localStorage.setItem("prescription", JSON.stringify(data));
    alert("Prescription saved successfully ✅");
  };

  return (
    <div>
      <style>{css}</style>

      <div className="page">
        <h2>💊 Write Prescription</h2>

        <div className="card">
          <input
            placeholder="Patient Name"
            value={patientName}
            onChange={e => setPatientName(e.target.value)}
          />

          <textarea
            placeholder="Diagnosis"
            value={diagnosis}
            onChange={e => setDiagnosis(e.target.value)}
          />

          <table>
            <thead>
              <tr>
                <th>Medicine</th>
                <th>Dosage</th>
                <th>Duration</th>
                <th>Notes</th>
              </tr>
            </thead>
            <tbody>
              {medicines.map((m, i) => (
                <tr key={i}>
                  <td><input onChange={e=>handleChange(i,"name",e.target.value)} /></td>
                  <td><input onChange={e=>handleChange(i,"dose",e.target.value)} /></td>
                  <td><input onChange={e=>handleChange(i,"duration",e.target.value)} /></td>
                  <td><input onChange={e=>handleChange(i,"notes",e.target.value)} /></td>
                </tr>
              ))}
            </tbody>
          </table>

          <button onClick={addRow} className="btn2">+ Add Medicine</button>
          <button onClick={savePrescription} className="btn">Save Prescription</button>
        </div>
      </div>
    </div>
  );
}

const css = `
.page {
  padding: 30px;
  color: white;
  background: #0b132b;
  min-height: 100vh;
  font-family: Arial;
}

.card {
  background: linear-gradient(135deg, #0f3460, #16213e);
  padding: 25px;
  border-radius: 20px;
  max-width: 900px;
  box-shadow: 0 0 20px rgba(0,0,0,0.5);
}

input, textarea {
  width: 100%;
  padding: 10px;
  margin-bottom: 12px;
  border-radius: 8px;
  border: none;
}

textarea {
  height: 80px;
}

table {
  width: 100%;
  border-collapse: collapse;
  margin: 15px 0;
}

th, td {
  border-bottom: 1px solid #334;
  padding: 8px;
  text-align: left;
}

.btn {
  background: linear-gradient(90deg, #00c6ff, #0072ff);
  border: none;
  padding: 12px 20px;
  color: white;
  border-radius: 10px;
  cursor: pointer;
  font-weight: bold;
}

.btn2 {
  background: #444;
  margin-right: 10px;
  border: none;
  padding: 12px 15px;
  color: white;
  border-radius: 10px;
  cursor: pointer;
}
`;
