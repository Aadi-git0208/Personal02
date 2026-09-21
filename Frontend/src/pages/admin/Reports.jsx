import React from "react";
import "./Admin.css";

const Reports = () => {
  const feedbacks = JSON.parse(localStorage.getItem("feedbacks")) || [];

  return (
    <div className="admin-page">
      <h1>Reports</h1>

      <div className="admin-section">
        <h2>Patient Feedback</h2>

        {feedbacks.length === 0 ? (
          <p className="admin-empty">No feedback available</p>
        ) : (
          feedbacks.map((f) => (
            <div className="admin-row" key={f.id}>
              <span>{f.message}</span>
            </div>
          ))
        )}
      </div>
    </div>
  );
};

export default Reports;
