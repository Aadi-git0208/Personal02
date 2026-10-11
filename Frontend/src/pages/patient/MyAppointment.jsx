import { useEffect, useState } from "react";
import { getApiErrorMessage, pageContent } from "../../api/client";
import { cancelAppointment, listPatientAppointments } from "../../api/appointmentApi";

function statusLabel(status) {
    switch (status) {
        case "PENDING":
            return "Pending Approval";
        case "CONFIRMED":
            return "Appointment Accepted";
        case "REJECTED":
            return "Appointment Rejected";
        case "CANCELLED":
            return "Cancelled";
        case "COMPLETED":
            return "Completed";
        default:
            return status || "Unknown";
    }
}

function statusClass(status) {
    if (status === "PENDING") return "pending";
    if (status === "CONFIRMED" || status === "COMPLETED") return "confirmed";
    return "cancelled";
}

function MyAppointment() {
    const [appointments, setAppointments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [pendingId, setPendingId] = useState(null);

    async function loadAppointments() {
        setLoading(true);
        setError("");
        try {
            const page = await listPatientAppointments({ size: 50, sort: "appointmentDate,desc" });
            setAppointments(pageContent(page));
        } catch (loadError) {
            setError(getApiErrorMessage(loadError));
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        loadAppointments();
    }, []);

    async function handleCancel(appointmentId) {
        setPendingId(appointmentId);
        setError("");
        try {
            await cancelAppointment(appointmentId, "Cancelled by patient");
            await loadAppointments();
        } catch (cancelError) {
            setError(getApiErrorMessage(cancelError));
        } finally {
            setPendingId(null);
        }
    }

    if (loading) {
        return (
            <div className="no-appointments">
                <h3>Loading appointments...</h3>
            </div>
        );
    }

    if (error) {
        return (
            <div className="no-appointments">
                <h3>Unable to load appointments</h3>
                <p>{error}</p>
            </div>
        );
    }

    if (appointments.length === 0) {
        return (
            <div className="no-appointments">
                <h3>No Appointment Found</h3>
                <p>You have not booked any appointments yet.</p>
            </div>
        );
    }

    return (
        <div className="my-appointments">
            <h2 className="appointments-title">My Appointments</h2>
            <div className="appointments-list">
                {appointments.map((app) => (
                    <div className="appointment-card" key={app.id}>
                        <div className="appointment-details">
                            <p>
                                <strong>Doctor:</strong> {app.doctorFullName || "N/A"}
                            </p>
                            <p>
                                <strong>Specialization:</strong> {app.doctorSpecialization || "N/A"}
                            </p>
                            <p>
                                <strong>Time Slot:</strong> {app.slotLabel || `${app.startTime}–${app.endTime}`}
                            </p>
                            <p>
                                <strong>Date:</strong> {app.appointmentDate || "N/A"}
                            </p>
                            <p>
                                <strong>Reason:</strong> {app.reason || "N/A"}
                            </p>
                        </div>
                        <div className="appointment-status">
                            <p className={`status ${statusClass(app.status)}`}>
                                {statusLabel(app.status)}
                            </p>
                            {(app.status === "PENDING" || app.status === "CONFIRMED") && (
                                <button
                                    type="button"
                                    disabled={pendingId === app.id}
                                    onClick={() => handleCancel(app.id)}
                                >
                                    {pendingId === app.id ? "Cancelling..." : "Cancel"}
                                </button>
                            )}
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
}

export default MyAppointment;
