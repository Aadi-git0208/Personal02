import React, { useEffect, useState } from "react";
import { getApiErrorMessage, pageContent } from "../../api/client";
import {
    completeAppointment,
    confirmAppointment,
    listDoctorAppointments,
    rejectAppointment
} from "../../api/appointmentApi";
import "./DoctorDashboard.css";

function statusClass(status) {
    const value = String(status || "").toLowerCase();
    if (value === "pending") return "pending";
    if (value === "confirmed" || value === "completed") return "confirmed";
    return "cancelled";
}

const DoctorAppointments = () => {
    const [appointments, setAppointments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [pendingId, setPendingId] = useState(null);

    async function loadAppointments() {
        setLoading(true);
        setError("");
        try {
            const page = await listDoctorAppointments({ size: 50, sort: "appointmentDate,desc" });
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

    async function runAction(appointmentId, action) {
        setPendingId(appointmentId);
        setError("");
        try {
            await action();
            await loadAppointments();
        } catch (actionError) {
            setError(getApiErrorMessage(actionError));
        } finally {
            setPendingId(null);
        }
    }

    return (
        <div className="doctor-dashboard no-sidebar">
            <main className="main-content">
                <h1>My Appointments</h1>
                <p className="subtitle">Manage your patient appointments</p>
                {error && <p role="alert">{error}</p>}
                {loading ? (
                    <div style={{ textAlign: "center", padding: "40px" }}>
                        <p style={{ opacity: 0.7 }}>Loading appointments...</p>
                    </div>
                ) : appointments.length === 0 ? (
                    <div style={{ textAlign: "center", padding: "40px" }}>
                        <p style={{ opacity: 0.7 }}>No appointments yet</p>
                    </div>
                ) : (
                    <div className="table-card">
                        <table>
                            <thead>
                                <tr>
                                    <th>Patient</th>
                                    <th>Date</th>
                                    <th>Time</th>
                                    <th>Reason</th>
                                    <th>Status</th>
                                    <th>Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                {appointments.map((app) => (
                                    <tr key={app.id}>
                                        <td>Patient #{app.patientUserId}</td>
                                        <td>{app.appointmentDate}</td>
                                        <td>{app.slotLabel || `${app.startTime}–${app.endTime}`}</td>
                                        <td>{app.reason}</td>
                                        <td>
                                            <span className={`status ${statusClass(app.status)}`}>
                                                {app.status}
                                            </span>
                                        </td>
                                        <td>
                                            {app.status === "PENDING" && (
                                                <>
                                                    <button
                                                        className="btn accept"
                                                        disabled={pendingId === app.id}
                                                        onClick={() => runAction(app.id, () => confirmAppointment(app.id))}
                                                    >
                                                        Accept
                                                    </button>
                                                    <button
                                                        className="btn reject"
                                                        disabled={pendingId === app.id}
                                                        onClick={() => runAction(app.id, () => rejectAppointment(app.id, "Rejected by doctor"))}
                                                    >
                                                        Reject
                                                    </button>
                                                </>
                                            )}
                                            {app.status === "CONFIRMED" && (
                                                <button
                                                    className="btn accept"
                                                    disabled={pendingId === app.id}
                                                    onClick={() => runAction(app.id, () => completeAppointment(app.id))}
                                                >
                                                    Complete
                                                </button>
                                            )}
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </main>
        </div>
    );
};

export default DoctorAppointments;
