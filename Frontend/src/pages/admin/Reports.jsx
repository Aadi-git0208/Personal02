import React from "react";
import "./Admin.css";

const Reports = () => {
    return (
        <div className="admin-page">
            <h1>Reports</h1>
            <div className="admin-section">
                <h2>Patient Feedback</h2>
                <p className="admin-empty">
                    No reports API is available in the current backend. This screen is not connected to a service.
                </p>
            </div>
        </div>
    );
};

export default Reports;
