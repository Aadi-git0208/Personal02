import { useNavigate } from "react-router-dom";

function PatientDashboard() {
  const navigate = useNavigate(); 

  return (
    <div>
      <h2>Patient Dashboard</h2>

      <button onClick={() => navigate("/patient/book-appointment")}>
        Book Appointment
      </button>
    </div>
  );
}

export default PatientDashboard;
