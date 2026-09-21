import React from "react";
import { useNavigate } from "react-router-dom";
import "./DoctorDashboard.css";

const DoctorAppointments = () => {
  const navigate = useNavigate();
  const doctor = JSON.parse(localStorage.getItem("currentUser")) || {};

  const allAppointments = JSON.parse(localStorage.getItem("appointments")) || [];
  
  // Filter appointments for this doctor
  const appointments = allAppointments.filter(
    (app) =>
      app.doctorId === doctor.id ||
      app.doctorId === doctor.userId ||
      app.doctorUserId === doctor.id ||
      app.doctorUserId === doctor.userId
  );

  const updateStatus = (id, status) => {
    const updated = allAppointments.map((app) =>
      app.id === id ? { ...app, status } : app
    );

    localStorage.setItem("appointments", JSON.stringify(updated));
    window.location.reload();
  };

  return (
    <div className="doctor-dashboard no-sidebar">
      <main className="main-content">
        <h1>My Appointments</h1>
        <p className="subtitle">Manage your patient appointments</p>

        {appointments.length === 0 ? (
          <div style={{ textAlign: "center", padding: "40px" }}>
            <p style={{ opacity: 0.7 }}>No appointments yet</p>
          </div>
        ) : (
          <div className="table-card">
            <table>
              <thead>
                <tr>
                  <th>Patient</th>
                  <th>Date</th>
                  <th>Time</th>
                  <th>Status</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {appointments.map((app) => (
                  <tr key={app.id}>
                    <td>{app.patientName}</td>
                    <td>{app.date}</td>
                    <td>{app.time || app.slot}</td>
                    <td>
                      <span className={`status ${app.status}`}>
                        {app.status}
                      </span>
                    </td>
                    <td>
                      {app.status === "pending" && (
                        <>
                          <button
                            className="btn accept"
                            onClick={() => updateStatus(app.id, "confirmed")}
                          >
                            Accept
                          </button>
                          <button
                            className="btn reject"
                            onClick={() => updateStatus(app.id, "cancelled")}
                          >
                            Reject
                          </button>
                        </>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </main>
    </div>
  );
};

export default DoctorAppointments;
