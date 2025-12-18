import { useState } from "react";
import { useNavigate } from "react-router-dom";
import "./BookAppointment.css";

const doctors = [
  {
    id: 1,
    name: "Dr. Peter Doe",
    specialization: "Dentist",
    experience: "13 Years",
    fee: 500,
    image: "https://i.pravatar.cc/150?img=11",
  },
  {
    id: 2,
    name: "Dr. Sarah William",
    specialization: "Orthodontist",
    experience: "8 Years",
    fee: 800,
    image: "https://i.pravatar.cc/150?img=32",
  },
  {
    id: 3,
    name: "Dr. John Smith",
    specialization: "Cardiologist",
    experience: "10 Years",
    fee: 1000,
    image: "https://i.pravatar.cc/150?img=45",
  },
  {
    id: 4,
    name: "Dr. Nandani Sharma",
    specialization: "Surgeon",
    experience: "5 Years",
    fee: 1200,
    image: "/Nandani.jpg",
  },
  {
    id: 5,
    name: "Dr. Rahul Verma",
    specialization: "General Physician",
    experience: "7 Years",
    fee: 400,
    image: "/Rahul.jpg",
  },
  {
    id: 6,
    name: "Dr. Anjali Mehta",
    specialization: "Gynecologist",
    experience: "9 Years",
    fee: 900,
    image: "/Anjali.jpg",
  },
  {
    id: 7,
    name: "Dr. Amit Kapoor",
    specialization: "Neurologist",
    experience: "12 Years",
    fee: 1500,
    image: "/Amit.jpg",
  },
  {
    id: 8,
    name: "Dr. Priya Sharma",
    specialization: "Dermatologist",
    experience: "6 Years",
    fee: 700,
    image: "/Priya.jpg",
  },
  {
    id: 9,
    name: "Dr. Rakesh Iyer",
    specialization: "Orthopedic",
    experience: "11 Years",
    fee: 1100,
    image: "/Rakesh.jpg",
  },
  {
    id: 10,
    name: "Dr. Sneha Kulkarni",
    specialization: "Pediatrician",
    experience: "8 Years",
    fee: 600,
    image: "/Sneha.jpg",
  },
];

const timeSlots = [
  "10:00 AM",
  "11:00 AM",
  "12:00 PM",
  "02:00 PM",
  "04:00 PM",
];

function BookAppointment() {
  const navigate = useNavigate();

  const [selectedDoctor, setSelectedDoctor] = useState(null);
  const [patientName, setPatientName] = useState("");
  const [patientAge, setPatientAge] = useState("");
  const [slot, setSlot] = useState("");

  const confirmAppointment = () => {
    if (!patientName || !patientAge || !slot) {
      alert("Please fill all details");
      return;
    }

    const appointment = {
      id: Date.now(),
      doctor: selectedDoctor.name,
      specialization: selectedDoctor.specialization,
      fee: selectedDoctor.fee,
      patientName,
      patientAge,
      slot,
      date: new Date().toLocaleDateString(),
    };

    const existing =
      JSON.parse(localStorage.getItem("appointments")) || [];

    localStorage.setItem(
      "appointments",
      JSON.stringify([...existing, appointment])
    );

    navigate("/patient/my-appointment");
  };

  return (
  <div>
    <h2 className="page-title">Book Appointment</h2>

    {/* DOCTOR LIST */}
    {!selectedDoctor && (
      <div className="doctor-grid">
        {doctors.map((doc) => (
          <div className="doctor-card" key={doc.id}>
            <img src={doc.image} alt={doc.name} />
            <h3>{doc.name}</h3>
            <p className="specialization">{doc.specialization}</p>
            <p className="info">Experience: {doc.experience}</p>
            <p className="info">Fee: ₹{doc.fee}</p>

            <button onClick={() => setSelectedDoctor(doc)}>
              Book Appointment
            </button>
          </div>
        ))}
      </div>
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
            {timeSlots.map((t) => (
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
