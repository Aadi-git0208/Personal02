function MyAppointment() {
  const appointments =
    JSON.parse(localStorage.getItem("appointments")) || [];

  if (appointments.length === 0) {
    return <h3>No Appointment Found</h3>;
  }

  return (
    <div>
      <h2>My Appointments</h2>

      {appointments.map((app) => (
        <div className="appointment-card" key={app.id}>

          <p><strong>Patient:</strong> {app.patientName}</p>
          <p><strong>Doctor:</strong> {app.doctor}</p>
          <p><strong>Specialization:</strong> {app.specialization}</p>
          <p><strong>Time Slot:</strong> {app.slot}</p>
          <p><strong>Date:</strong> {app.date}</p>
          <p><strong>Fee:</strong> ₹{app.fee}</p>

          {/* ✅ STATUS MESSAGE */}
          {app.status === "pending" && (
            <p className="status pending">Pending Approval</p>
          )}

          {app.status === "confirmed" && (
            <p className="status confirmed">Appointment Accepted</p>
          )}

          {app.status === "cancelled" && (
            <p className="status cancelled">Appointment Rejected</p>
          )}
        </div>
      ))}
    </div>
  );
}

export default MyAppointment;
