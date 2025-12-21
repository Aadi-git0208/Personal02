import React from "react";

const DoctorAppointments = () => {
  return (
    <div>
      <h2>Appointments</h2>

      <div className="appointment-card">
        <p><b>Patient:</b> Uthman ibn Hunaif</p>
        <p><b>Time:</b> 10:00 AM – 11:45 AM</p>
        <button className="accept">Accept</button>
        <button className="reject">Reject</button>
      </div>

      <div className="appointment-card">
        <p><b>Patient:</b> Khalid Ahmed</p>
        <p><b>Time:</b> 6:00 PM – 7:00 PM</p>
        <button className="accept">Accept</button>
        <button className="reject">Reject</button>
      </div>
    </div>
  );
};

export default DoctorAppointments;
