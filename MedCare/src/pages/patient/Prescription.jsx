import { useState } from "react";

export default function Prescription() {

  const [data] = useState(() => {
    const saved = localStorage.getItem("prescription");
    return saved ? JSON.parse(saved) : null;
  });

  return (
    <div>
      <style>{css}</style>

      <div className="page">
        <h2>📄 My Prescription</h2>

        {!data ? (
          <div className="empty">
            No prescription found ❌
          </div>
        ) : (
          <div className="card">

            <div className="info">
              <p><b>Patient:</b> {data.patientName}</p>
              <p><b>Doctor:</b> {data.doctor}</p>
              <p><b>Date:</b> {data.date}</p>
              <p><b>Diagnosis:</b> {data.diagnosis}</p>
            </div>

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
                {data.medicines.map((m, i) => (
                  <tr key={i}>
                    <td>{m.name}</td>
                    <td>{m.dose}</td>
                    <td>{m.duration}</td>
                    <td>{m.notes}</td>
                  </tr>
                ))}
              </tbody>
            </table>

          </div>
        )}
      </div>
    </div>
  );
}

const css = `
.page {
  padding: 30px;
  min-height: 100vh;
  background: #0b132b;
  color: white;
  font-family: Arial, sans-serif;
}

h2 {
  margin-bottom: 20px;
}

.card {
  background: linear-gradient(135deg, #0f3460, #16213e);
  padding: 25px;
  border-radius: 18px;
  max-width: 900px;
  box-shadow: 0 0 20px rgba(0,0,0,0.5);
}

.info p {
  margin: 6px 0;
}

table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 15px;
}

th, td {
  padding: 10px;
  border-bottom: 1px solid #334;
  text-align: left;
}

th {
  color: #00c6ff;
}

.empty {
  margin-top: 30px;
  padding: 20px;
  background: #1c2541;
  border-radius: 12px;
  text-align: center;
  color: #ffb703;
  font-weight: bold;
}
`;
