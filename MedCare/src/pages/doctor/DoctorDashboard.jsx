import React, { useEffect } from "react";
import { useNavigate, Link, Outlet, useLocation } from "react-router-dom";
import "./DoctorDashboard.css";

const DoctorDashboard = () => {
  const navigate = useNavigate();
  const location = useLocation();

  const allAppointments =
    JSON.parse(localStorage.getItem("appointments")) || [];

  const doctor =
    JSON.parse(localStorage.getItem("currentUser")) || {};

  // Filter appointments for this doctor
  const appointments = allAppointments.filter(
    (app) =>
      app.doctorId === doctor.id ||
      app.doctorId === doctor.userId ||
      app.doctorUserId === doctor.id ||
      app.doctorUserId === doctor.userId
  );

  useEffect(() => {
    if (!doctor?.name) {
      navigate("/doctor/login");
    }
  }, [doctor?.name, navigate]);

  // Check if we're on a child route (not the main dashboard)
  const isChildRoute = location.pathname !== '/doctor' && location.pathname !== '/doctor/';

  const pending = appointments.filter(a => a.status === "pending");
  const confirmed = appointments.filter(a => a.status === "confirmed");

  const updateStatus = (id, status) => {
    const updated = appointments.map(app =>
      app.id === id ? { ...app, status } : app
    );

    localStorage.setItem("appointments", JSON.stringify(updated));
    window.location.reload();
  };

  const handleLogout = () => {
    localStorage.removeItem("currentUser");
    navigate("/doctor/login");
  };


  return (
    <div className="doctor-dashboard no-sidebar">
      {/* Only show dashboard content if we're on the main dashboard route */}
      {!isChildRoute && (
        <main className="main-content">
          {/* ===== HEADER ===== */}
          <h1>
            Welcome, {doctor?.name || "Doctor"} 👨‍⚕️
          </h1>
          <p className="subtitle">Here is your daily overview</p>

          {/* ===== FEATURE CARDS ===== */}
          <div className="feature-grid">
            <div className="feature-card active">
              <h3>📅 Book Appointment</h3>
              <p>Find doctors & book appointments</p>
            </div>

            <div className="feature-card">
              <h3>🗂 My Appointments</h3>
              <p>View appointment history</p>
            </div>

            {/* ✅ CLICKABLE MESSAGES CARD */}
            <Link to="chat" className="feature-card">
              <h3>💬 Messages</h3>
              <p>Patients Interaction</p>
            </Link>

            <div className="feature-card">
              <h3>💊 Pharmacy</h3>
              <p>Order medicines online</p>
            </div>
          </div>

          {/* ===== STATS ===== */}
          <div className="stats">
            <div className="card">
              <h3>Total Appointments</h3>
              <p>{appointments.length}</p>
            </div>
            <div className="card">
              <h3>Pending</h3>
              <p>{pending.length}</p>
            </div>
            <div className="card">
              <h3>Confirmed</h3>
              <p>{confirmed.length}</p>
            </div>
          </div>

          {/* ===== RECENT APPOINTMENTS ===== */}
          <div className="table-card">
            <h2>Recent Appointments</h2>

            {appointments.length === 0 ? (
              <p>No appointments yet</p>
            ) : (
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
                  {appointments.map(app => (
                    <tr key={app.id}>
                      <td>{app.patientName}</td>
                      <td>{app.date}</td>
                      <td>{app.time}</td>
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
                              onClick={() =>
                                updateStatus(app.id, "confirmed")
                              }
                            >
                              Accept
                            </button>
                            <button
                              className="btn reject"
                              onClick={() =>
                                updateStatus(app.id, "cancelled")
                              }
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
            )}
          </div>
        </main>
      )}

      {/* Render child routes (chat, appointments, etc.) */}
      <Outlet />
    </div>
  );
};

export default DoctorDashboard;
