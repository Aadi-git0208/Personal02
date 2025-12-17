import { useState } from "react";
import doctors from "../../data/doctors";

import { useNavigate } from "react-router-dom";

function BookAppointment() {
  const [selectedDoctor, setSelectedDoctor] = useState(null);
  const navigate = useNavigate();

  const slots = ["10:00 AM", "10:30 AM", "11:00 AM"];

  const bookSlot = (doctor, time) => {
    navigate("/patient/my-appointment", {
      state: {
        doctor: doctor.name,
        time,
        fee: doctor.fee
      }
    });
  };

  return (
    <div>
      <h2>Book Appointment</h2>

      {!selectedDoctor && doctors.map(doc => (
        <div key={doc.id} className="card">
          <h3>{doc.name}</h3>
          <p>{doc.specialization}</p>
          <p>{doc.experience}</p>
          <p>Fee: ₹{doc.fee}</p>
          <button onClick={() => setSelectedDoctor(doc)}>
            Select Doctor
          </button>
        </div>
      ))}

      {selectedDoctor && (
        <>
          <h3>Select Time Slot for {selectedDoctor.name}</h3>
          {slots.map(slot => (
            <button
              key={slot}
              onClick={() => bookSlot(selectedDoctor, slot)}
            >
              {slot}
            </button>
          ))}
        </>
      )}
    </div>
  );
}

export default BookAppointment;
