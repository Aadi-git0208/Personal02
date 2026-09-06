import React, { useEffect } from "react";
import {
    useNavigate,
    Link,
    Outlet,
    useLocation
} from "react-router-dom";
import "./DoctorDashboard.css";

const DoctorDashboard = () => {
    const navigate = useNavigate();
    const location = useLocation();

    const allAppointments =
        JSON.parse(
            localStorage.getItem("appointments")
        ) || [];

    const doctor =
        JSON.parse(
            localStorage.getItem("currentUser")
        ) || {};

    const appointments =
        allAppointments.filter(
            (appointment) =>
                appointment.doctorId === doctor.id ||
                appointment.doctorId === doctor.userId ||
                appointment.doctorUserId === doctor.id ||
                appointment.doctorUserId === doctor.userId
        );

    useEffect(() => {
        if (!doctor?.name) {
            navigate("/doctor/login");
        }
    }, [doctor?.name, navigate]);

    const isChildRoute =
        location.pathname !== "/doctor" &&
        location.pathname !== "/doctor/";

    const pending =
        appointments.filter(
            (appointment) =>
                appointment.status === "pending"
        );

    const confirmed =
        appointments.filter(
            (appointment) =>
                appointment.status === "confirmed"
        );

    const updateStatus = (id, status) => {
        const updatedAppointments =
            allAppointments.map(
                (appointment) =>
                    appointment.id === id
                        ? {
                              ...appointment,
                              status: status
                          }
                        : appointment
            );

        localStorage.setItem(
            "appointments",
            JSON.stringify(
                updatedAppointments
            )
        );

        window.location.reload();
    };

    return (
        <div className="doctor-dashboard no-sidebar">
            {!isChildRoute && (
                <main className="main-content">
                    <h1>
                        Welcome,{" "}
                        {doctor?.name ||
                            "Doctor"}
                    </h1>

                    <p className="subtitle">
                        Here is your daily overview
                    </p>

                    <div className="feature-grid">
                        <div
                            className="feature-card feature-card--active"
                            onClick={() =>
                                navigate(
                                    "/doctor/prescription"
                                )
                            }
                        >
                            <div className="feature-icon">
                                📄
                            </div>

                            <h3>
                                Prescription
                            </h3>

                            <p>
                                Write the prescription
                                for patients
                            </p>
                        </div>

                        <div
                            className="feature-card"
                            onClick={() =>
                                navigate(
                                    "/doctor/patients"
                                )
                            }
                        >
                            <div className="feature-icon">
                                👥
                            </div>

                            <h3>
                                My Patients
                            </h3>

                            <p>
                                View patient profiles
                            </p>
                        </div>

                        <Link
                            to="chat"
                            className="feature-card"
                        >
                            <div className="feature-icon">
                                💬
                            </div>

                            <h3>
                                Messages
                            </h3>

                            <p>
                                Patients Interaction
                            </p>
                        </Link>

                        <div className="feature-card">
                            <div className="feature-icon">
                                💊
                            </div>

                            <h3>
                                Pharmacy
                            </h3>

                            <p>
                                Order medicines online
                            </p>
                        </div>
                    </div>

                    <div className="stats">
                        <div className="card">
                            <h3>
                                Total Appointments
                            </h3>

                            <p>
                                {
                                    appointments.length
                                }
                            </p>
                        </div>

                        <div className="card">
                            <h3>
                                Pending
                            </h3>

                            <p>
                                {pending.length}
                            </p>
                        </div>

                        <div className="card">
                            <h3>
                                Confirmed
                            </h3>

                            <p>
                                {confirmed.length}
                            </p>
                        </div>
                    </div>

                    <div className="table-card">
                        <h2>
                            Recent Appointments
                        </h2>

                        {appointments.length ===
                        0 ? (
                            <p className="no-appointments">
                                No appointments yet
                            </p>
                        ) : (
                            <div className="table-wrapper">
                                <table>
                                    <thead>
                                        <tr>
                                            <th>
                                                Patient
                                            </th>
                                            <th>
                                                Date
                                            </th>
                                            <th>
                                                Time
                                            </th>
                                            <th>
                                                Status
                                            </th>
                                            <th>
                                                Action
                                            </th>
                                        </tr>
                                    </thead>

                                    <tbody>
                                        {appointments.map(
                                            (
                                                appointment
                                            ) => (
                                                <tr
                                                    key={
                                                        appointment.id
                                                    }
                                                >
                                                    <td>
                                                        {
                                                            appointment.patientName
                                                        }
                                                    </td>

                                                    <td>
                                                        {
                                                            appointment.date
                                                        }
                                                    </td>

                                                    <td>
                                                        {
                                                            appointment.time
                                                        }
                                                    </td>

                                                    <td>
                                                        <span
                                                            className={`status ${appointment.status}`}
                                                        >
                                                            {
                                                                appointment.status
                                                            }
                                                        </span>
                                                    </td>

                                                    <td>
                                                        {appointment.status ===
                                                            "pending" && (
                                                            <div className="action-buttons">
                                                                <button
                                                                    className="btn accept"
                                                                    onClick={() =>
                                                                        updateStatus(
                                                                            appointment.id,
                                                                            "confirmed"
                                                                        )
                                                                    }
                                                                >
                                                                    Accept
                                                                </button>

                                                                <button
                                                                    className="btn reject"
                                                                    onClick={() =>
                                                                        updateStatus(
                                                                            appointment.id,
                                                                            "cancelled"
                                                                        )
                                                                    }
                                                                >
                                                                    Reject
                                                                </button>
                                                            </div>
                                                        )}
                                                    </td>
                                                </tr>
                                            )
                                        )}
                                    </tbody>
                                </table>
                            </div>
                        )}
                    </div>
                </main>
            )}

            <Outlet />
        </div>
    );
};

export default DoctorDashboard;