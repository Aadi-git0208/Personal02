import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "./BookAppointment.css";

const timeSlots = [
    "10:00 AM",
    "11:00 AM",
    "12:00 PM",
    "02:00 PM",
    "04:00 PM"
];

function BookAppointment() {
    const navigate = useNavigate();

    const patient =
        JSON.parse(localStorage.getItem("currentUser")) || {};

    const [doctors, setDoctors] = useState([]);
    const [selectedDoctor, setSelectedDoctor] = useState(null);
    const [patientName, setPatientName] = useState(
        patient.name || ""
    );
    const [patientAge, setPatientAge] = useState("");
    const [slot, setSlot] = useState("");

    useEffect(() => {
        const loadDoctors = () => {
            try {
                const doctorProfiles =
                    JSON.parse(
                        localStorage.getItem("doctorProfiles")
                    ) || [];

                setDoctors(doctorProfiles);
            } catch (error) {
                console.error(
                    "Error loading doctors:",
                    error
                );
            }
        };

        loadDoctors();

        const interval = setInterval(loadDoctors, 1000);

        window.addEventListener(
            "storage",
            loadDoctors
        );

        return () => {
            clearInterval(interval);

            window.removeEventListener(
                "storage",
                loadDoctors
            );
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
            createdAt: new Date().toISOString()
        };

        const existing =
            JSON.parse(
                localStorage.getItem("appointments")
            ) || [];

        localStorage.setItem(
            "appointments",
            JSON.stringify([
                ...existing,
                appointment
            ])
        );

        alert("Appointment booked successfully!");

        navigate("/patient/my-appointment");
    };

    return (
        <div className="book-appointment-page">
            <h2 className="page-title">
                Book Appointment
            </h2>

            {!selectedDoctor && (
                <>
                    {doctors.length === 0 ? (
                        <div className="no-doctors">
                            <p className="no-doctors-title">
                                No doctors available at the
                                moment.
                            </p>

                            <p className="no-doctors-subtitle">
                                Please check back later.
                            </p>
                        </div>
                    ) : (
                        <div className="doctor-grid">
                            {doctors.map((doc) => (
                                <div
                                    className="doctor-card"
                                    key={doc.id}
                                >
                                    {doc.image ? (
                                        <img
                                            src={doc.image}
                                            alt={doc.name}
                                        />
                                    ) : (
                                        <div className="doctor-placeholder">
                                            {doc.name
                                                ?.charAt(0)
                                                .toUpperCase()}
                                        </div>
                                    )}

                                    <h3>
                                        {doc.name}
                                    </h3>

                                    <p className="specialization">
                                        {doc.specialization}
                                    </p>

                                    <p className="info">
                                        Experience:{" "}
                                        {doc.experience}
                                    </p>

                                    <p className="info">
                                        Fee: ₹{doc.fee}
                                    </p>

                                    {doc.timeSlots &&
                                        doc.timeSlots.length >
                                            0 && (
                                            <p className="info available-slots">
                                                Available:{" "}
                                                {doc.timeSlots.join(
                                                    ", "
                                                )}
                                            </p>
                                        )}

                                    <button
                                        onClick={() =>
                                            setSelectedDoctor(
                                                doc
                                            )
                                        }
                                    >
                                        Book Appointment
                                    </button>
                                </div>
                            ))}
                        </div>
                    )}
                </>
            )}

            {selectedDoctor && (
                <div className="booking-wrapper">
                    <div className="booking-form">
                        <h3>
                            {selectedDoctor.name}
                        </h3>

                        <input
                            type="text"
                            placeholder="Patient Name"
                            value={patientName}
                            onChange={(e) =>
                                setPatientName(
                                    e.target.value
                                )
                            }
                        />

                        <input
                            type="number"
                            placeholder="Patient Age"
                            value={patientAge}
                            onChange={(e) =>
                                setPatientAge(
                                    e.target.value
                                )
                            }
                        />

                        <select
                            value={slot}
                            onChange={(e) =>
                                setSlot(e.target.value)
                            }
                        >
                            <option value="">
                                Select Time Slot
                            </option>

                            {selectedDoctor?.timeSlots &&
                            selectedDoctor.timeSlots
                                .length > 0
                                ? selectedDoctor.timeSlots.map(
                                      (time) => (
                                          <option
                                              key={time}
                                              value={time}
                                          >
                                              {time}
                                          </option>
                                      )
                                  )
                                : timeSlots.map(
                                      (time) => (
                                          <option
                                              key={time}
                                              value={time}
                                          >
                                              {time}
                                          </option>
                                      )
                                  )}
                        </select>

                        <button
                            onClick={confirmAppointment}
                        >
                            Confirm Appointment
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
}

export default BookAppointment;