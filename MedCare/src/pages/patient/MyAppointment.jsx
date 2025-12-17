import { useLocation } from "react-router-dom";

function MyAppointment() {
  const { state } = useLocation();

  if (!state) {
    return <h3>No Appointment Found</h3>;
  }

  return (
    <div>
      <h2>✅ Appointment Booked Successfully</h2>
      <p><strong>Doctor:</strong> {state.doctor}</p>
      <p><strong>Time:</strong> {state.time}</p>
      <p><strong>Consultation Fee:</strong> ₹{state.fee}</p>

      <button>Download Receipt</button>
    </div>
  );
}

export default MyAppointment;
