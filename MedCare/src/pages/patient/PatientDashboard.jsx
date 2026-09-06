import { Link, Outlet } from "react-router-dom";
import "./PatientDashboard.css";

function PatientDashboard() {
    const user = JSON.parse(localStorage.getItem("currentUser"));

    return (
        <>
            <section className="patient-dashboard">
                <div className="dashboard-header">
                    <h2>
                        Welcome, {user?.name || "Patient"}
                    </h2>
                </div>

                <div className="dashboard-boxes">
                    <Link
                        to="/patient/book-appointment"
                        className="dashboard-box"
                    >
                        <h3>
                            <span className="dashboard-icon">
                                <svg
                                    width="22"
                                    height="22"
                                    viewBox="0 0 24 24"
                                    fill="none"
                                    xmlns="http://www.w3.org/2000/svg"
                                >
                                    <path
                                        d="M19 3H5C3.9 3 3 3.9 3 5V19C3 20.1 3.9 21 5 21H19C20.1 21 21 20.1 21 19V5C21 3.9 20.1 3 19 3Z"
                                        stroke="currentColor"
                                        strokeWidth="2"
                                    />
                                    <path
                                        d="M9 8L7 10L9 12"
                                        stroke="currentColor"
                                        strokeWidth="2"
                                        strokeLinecap="round"
                                        strokeLinejoin="round"
                                    />
                                    <path
                                        d="M15 8L17 10L15 12"
                                        stroke="currentColor"
                                        strokeWidth="2"
                                        strokeLinecap="round"
                                        strokeLinejoin="round"
                                    />
                                </svg>
                            </span>
                            Book Appointment
                        </h3>

                        <p>
                            Find doctors & book appointments
                        </p>
                    </Link>

                    <Link
                        to="/patient/my-appointment"
                        className="dashboard-box"
                    >
                        <h3>
                            <span className="dashboard-icon">
                                <svg
                                    width="22"
                                    height="22"
                                    viewBox="0 0 24 24"
                                    fill="none"
                                    xmlns="http://www.w3.org/2000/svg"
                                >
                                    <rect
                                        x="3"
                                        y="5"
                                        width="18"
                                        height="16"
                                        rx="2"
                                        stroke="currentColor"
                                        strokeWidth="2"
                                    />
                                    <path
                                        d="M16 3V7"
                                        stroke="currentColor"
                                        strokeWidth="2"
                                        strokeLinecap="round"
                                    />
                                    <path
                                        d="M8 3V7"
                                        stroke="currentColor"
                                        strokeWidth="2"
                                        strokeLinecap="round"
                                    />
                                    <path
                                        d="M3 10H21"
                                        stroke="currentColor"
                                        strokeWidth="2"
                                    />
                                    <path
                                        d="M8 14H10"
                                        stroke="currentColor"
                                        strokeWidth="2"
                                        strokeLinecap="round"
                                    />
                                    <path
                                        d="M14 14H16"
                                        stroke="currentColor"
                                        strokeWidth="2"
                                        strokeLinecap="round"
                                    />
                                </svg>
                            </span>
                            My Appointments
                        </h3>

                        <p>
                            View appointment history
                        </p>
                    </Link>

                    <Link
                        to="/patient/chat"
                        className="dashboard-box"
                    >
                        <h3>
                            <span className="dashboard-icon">
                                <svg
                                    width="22"
                                    height="22"
                                    viewBox="0 0 24 24"
                                    fill="none"
                                    xmlns="http://www.w3.org/2000/svg"
                                >
                                    <path
                                        d="M20 11.5C20 16.19 16.19 20 11.5 20C10.14 20 8.86 19.68 7.73 19.12L4 20L4.88 16.27C4.32 15.14 4 13.86 4 12.5C4 7.81 7.81 4 12.5 4C17.19 4 20 7.81 20 11.5Z"
                                        stroke="currentColor"
                                        strokeWidth="2"
                                        strokeLinecap="round"
                                        strokeLinejoin="round"
                                    />
                                    <path
                                        d="M8 12H8.01"
                                        stroke="currentColor"
                                        strokeWidth="2"
                                        strokeLinecap="round"
                                    />
                                    <path
                                        d="M12 12H12.01"
                                        stroke="currentColor"
                                        strokeWidth="2"
                                        strokeLinecap="round"
                                    />
                                    <path
                                        d="M16 12H16.01"
                                        stroke="currentColor"
                                        strokeWidth="2"
                                        strokeLinecap="round"
                                    />
                                </svg>
                            </span>
                            Chat with Doctor
                        </h3>

                        <p>
                            Consult doctors online
                        </p>
                    </Link>

                    <Link
                        to="/patient/pharmacy"
                        className="dashboard-box"
                    >
                        <h3>
                            <span className="dashboard-icon">
                                <svg
                                    width="22"
                                    height="22"
                                    viewBox="0 0 24 24"
                                    fill="none"
                                    xmlns="http://www.w3.org/2000/svg"
                                >
                                    <path
                                        d="M8 3H16V7H8V3Z"
                                        stroke="currentColor"
                                        strokeWidth="2"
                                        strokeLinejoin="round"
                                    />
                                    <path
                                        d="M6 7H18C19.1 7 20 7.9 20 9V19C20 20.1 19.1 21 18 21H6C4.9 21 4 20.1 4 19V9C4 7.9 4.9 7 6 7Z"
                                        stroke="currentColor"
                                        strokeWidth="2"
                                    />
                                    <path
                                        d="M12 11V17"
                                        stroke="currentColor"
                                        strokeWidth="2"
                                        strokeLinecap="round"
                                    />
                                    <path
                                        d="M9 14H15"
                                        stroke="currentColor"
                                        strokeWidth="2"
                                        strokeLinecap="round"
                                    />
                                </svg>
                            </span>
                            Pharmacy
                        </h3>

                        <p>
                            Order medicines online
                        </p>
                    </Link>

                    <Link
                        to="/patient/prescription"
                        className="dashboard-box"
                    >
                        <h3>
                            <span className="dashboard-icon">
                                <svg
                                    width="22"
                                    height="22"
                                    viewBox="0 0 24 24"
                                    fill="none"
                                    xmlns="http://www.w3.org/2000/svg"
                                >
                                    <path
                                        d="M6 3H14L19 8V21H6C4.9 21 4 20.1 4 19V5C4 3.9 4.9 3 6 3Z"
                                        stroke="currentColor"
                                        strokeWidth="2"
                                        strokeLinejoin="round"
                                    />
                                    <path
                                        d="M14 3V8H19"
                                        stroke="currentColor"
                                        strokeWidth="2"
                                        strokeLinejoin="round"
                                    />
                                    <path
                                        d="M8 13H16"
                                        stroke="currentColor"
                                        strokeWidth="2"
                                        strokeLinecap="round"
                                    />
                                    <path
                                        d="M8 17H14"
                                        stroke="currentColor"
                                        strokeWidth="2"
                                        strokeLinecap="round"
                                    />
                                </svg>
                            </span>
                            Prescription
                        </h3>

                        <p>
                            View doctor prescriptions
                        </p>
                    </Link>
                </div>
            </section>

            <section className="patient-page-content">
                <Outlet />
            </section>
        </>
    );
}

export default PatientDashboard;