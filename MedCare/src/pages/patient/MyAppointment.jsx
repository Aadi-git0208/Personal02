function MyAppointment() {
    const appointments =
        JSON.parse(localStorage.getItem("appointments")) || [];

    if (appointments.length === 0) {
        return (
            <div className="no-appointments">
                <h3>No Appointment Found</h3>
                <p>
                    You have not booked any appointments yet.
                </p>
            </div>
        );
    }

    return (
        <div className="my-appointments">
            <h2 className="appointments-title">
                My Appointments
            </h2>

            <div className="appointments-list">
                {appointments.map((app) => (
                    <div
                        className="appointment-card"
                        key={app.id || app.appointmentId}
                    >
                        <div className="appointment-details">
                            <p>
                                <strong>Patient:</strong>{" "}
                                {app.patientName || "N/A"}
                            </p>

                            <p>
                                <strong>Doctor:</strong>{" "}
                                {app.doctorName ||
                                    app.doctor ||
                                    "N/A"}
                            </p>

                            <p>
                                <strong>
                                    Specialization:
                                </strong>{" "}
                                {app.specialization ||
                                    "N/A"}
                            </p>

                            <p>
                                <strong>
                                    Time Slot:
                                </strong>{" "}
                                {app.slot ||
                                    app.time ||
                                    "N/A"}
                            </p>

                            <p>
                                <strong>Date:</strong>{" "}
                                {app.date || "N/A"}
                            </p>

                            <p>
                                <strong>Fee:</strong> ₹
                                {app.fee || "0"}
                            </p>
                        </div>

                        <div className="appointment-status">
                            {app.status === "pending" && (
                                <p className="status pending">
                                    Pending Approval
                                </p>
                            )}

                            {app.status === "confirmed" && (
                                <p className="status confirmed">
                                    Appointment Accepted
                                </p>
                            )}

                            {app.status === "cancelled" && (
                                <p className="status cancelled">
                                    Appointment Rejected
                                </p>
                            )}
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
}

export default MyAppointment;