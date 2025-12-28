import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "./BookAppointment.css";

const timeSlots = [
  "10:00 AM",
  "11:00 AM",
  "12:00 PM",
  "02:00 PM",
  "04:00 PM",
];

function BookAppointment() {
  const navigate = useNavigate();
  const patient = JSON.parse(localStorage.getItem("currentUser")) || {};

  const [doctors, setDoctors] = useState([]);
  const [selectedDoctor, setSelectedDoctor] = useState(null);
  const [patientName, setPatientName] = useState(patient.name || "");
  const [patientAge, setPatientAge] = useState("");
  const [slot, setSlot] = useState("");

  // Load doctors from doctor profiles
  useEffect(() => {
    const loadDoctors = () => {
      try {
        const doctorProfiles = JSON.parse(localStorage.getItem("doctorProfiles")) || [];
        setDoctors(doctorProfiles);
      } catch (err) {
        console.error("Error loading doctors:", err);
      }
    };

    loadDoctors();

    // Listen for updates
    const interval = setInterval(loadDoctors, 1000);
    window.addEventListener("storage", loadDoctors);

    return () => {
      clearInterval(interval);
      window.removeEventListener("storage", loadDoctors);
    };
  }, []);

  const confirmAppointment = () => {
    if (!patientName || !patientAge || !slot) {
      alert("Please fill all details");
      return;
    }

    if (!patient?.id) {
      alert("Please login to book appointment");
      navigate("/login");
      return;
    }

    const appointment = {
      id: Date.now().toString(),
      appointmentId: Date.now().toString(),
      doctorId: selectedDoctor.id,
      doctorUserId: selectedDoctor.userId,
      doctorName: selectedDoctor.name,
      doctorEmail: selectedDoctor.email,
      specialization: selectedDoctor.specialization,
      fee: selectedDoctor.fee,
      patientId: patient.id,
      patientName: patientName,
      patientAge: patientAge,
      slot: slot,
      time: slot,
      date: new Date().toLocaleDateString(),
      status: "pending",
      createdAt: new Date().toISOString(),
    };

    const existing =
      JSON.parse(localStorage.getItem("appointments")) || [];

    localStorage.setItem(
      "appointments",
      JSON.stringify([...existing, appointment])
    );

    alert("Appointment booked successfully!");
    navigate("/patient/my-appointment");
  };


  return (
  <div>
    <h2 className="page-title">Book Appointment</h2>

    {/* DOCTOR LIST */}
    {!selectedDoctor && (
      <>
        {doctors.length === 0 ? (
          <div style={{ textAlign: "center", padding: "40px" }}>
            <p style={{ fontSize: "18px", opacity: 0.7 }}>
              No doctors available at the moment.
            </p>
            <p style={{ fontSize: "14px", opacity: 0.5, marginTop: "10px" }}>
              Please check back later.
            </p>
          </div>
        ) : (
          <div className="doctor-grid">
            {doctors.map((doc) => (
              <div className="doctor-card" key={doc.id}>
                {doc.image ? (
                  <img src={doc.image} alt={doc.name} />
                ) : (
                  <div
                    style={{
                      width: "100px",
                      height: "100px",
                      borderRadius: "50%",
                      background: "#ddd",
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "center",
                      margin: "0 auto",
                    }}
                  >
                    {doc.name.charAt(0)}
                  </div>
                )}
                <h3>{doc.name}</h3>
                <p className="specialization">{doc.specialization}</p>
                <p className="info">Experience: {doc.experience}</p>
                <p className="info">Fee: ₹{doc.fee}</p>
                {doc.timeSlots && doc.timeSlots.length > 0 && (
                  <p className="info" style={{ fontSize: "12px" }}>
                    Available: {doc.timeSlots.join(", ")}
                  </p>
                )}

                <button onClick={() => setSelectedDoctor(doc)}>
                  Book Appointment
                </button>
              </div>
            ))}
          </div>
        )}
      </>
    )}

    {/* BOOKING FORM (CENTERED) */}
    {selectedDoctor && (
      <div className="booking-wrapper">
        <div className="booking-form">
          <h3>{selectedDoctor.name}</h3>

          <input
            type="text"
            placeholder="Patient Name"
            value={patientName}
            onChange={(e) => setPatientName(e.target.value)}
          />

          <input
            type="number"
            placeholder="Patient Age"
            value={patientAge}
            onChange={(e) => setPatientAge(e.target.value)}
          />

          <select value={slot} onChange={(e) => setSlot(e.target.value)}>
            <option value="">Select Time Slot</option>
            {selectedDoctor?.timeSlots && selectedDoctor.timeSlots.length > 0
              ? selectedDoctor.timeSlots.map((t) => (
                  <option key={t} value={t}>
                    {t}
                  </option>
                ))
              : timeSlots.map((t) => (
                  <option key={t} value={t}>
                    {t}
                  </option>
                ))}
          </select>

          <button onClick={confirmAppointment}>
            Confirm Appointment
          </button>
        </div>
      </div>
    )}
  </div>
);

}

export default BookAppointment;
