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
      {/* TOP NAVBAR */}
      <header className="top-navbar">
        <div className="nav-left">
          <h2>MediCurex</h2>

          <nav>
            <Link to="book-appointment">Book Appointment</Link>
            <Link to="my-appointment">My Appointments</Link>
            <Link to="chat">Chat</Link>
            <Link to="pharmacy">Pharmacy</Link>
            <Link to="prescription">Prescription</Link>
          </nav>
        </div>

        <div className="nav-right">
          <span className="user-name">
            {user?.name || "Patient"}
          </span>
          <button onClick={logout}>Logout</button>
        </div>
      </header>

      {/* PAGE CONTENT */}
      <main className="page-content">
        <Outlet />
      </main>
    </>
  );
}

export default PatientDashboard;
