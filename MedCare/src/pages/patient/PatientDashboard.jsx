import { Link, Outlet, useNavigate } from "react-router-dom";
import "./PatientDashboard.css";

function PatientDashboard() {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem("currentUser"));

  const logout = () => {
    localStorage.removeItem("currentUser");
    localStorage.removeItem("isLoggedIn");
    navigate("/login");
  };

  return (
    <>
      {/* ===== DASHBOARD FEATURE BOXES ===== */}
      <section className="patient-dashboard">
        <div className="dashboard-header">
          <h2>Welcome, {user?.name || "Patient"}</h2>
          <button className="logout-btn" onClick={logout}>
            Logout
          </button>
        </div>

        <div className="dashboard-boxes">
          <Link to="book-appointment" className="dashboard-box">
            <h3>🩺 Book Appointment</h3>
            <p>Find doctors & book appointments</p>
          </Link>

          <Link to="my-appointment" className="dashboard-box">
            <h3>📅 My Appointments</h3>
            <p>View appointment history</p>
          </Link>

          <Link to="chat" className="dashboard-box">
            <h3>💬 Chat with Doctor</h3>
            <p>Consult doctors online</p>
          </Link>

          <Link to="pharmacy" className="dashboard-box">
            <h3>💊 Pharmacy</h3>
            <p>Order medicines online</p>
          </Link>

          <Link to="prescription" className="dashboard-box">
            <h3>📝 Prescription</h3>
            <p>View doctor prescriptions</p>
          </Link>
        </div>
      </section>

      {/* ===== PAGE CONTENT OPENS HERE ===== */}
      <section className="patient-page-content">
        <Outlet />
      </section>
    </>
  );
}

export default PatientDashboard;
