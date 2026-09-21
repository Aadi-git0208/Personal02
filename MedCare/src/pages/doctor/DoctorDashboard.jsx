import React, { useEffect, useState } from "react";
import { useNavigate, Link, Outlet, useLocation } from "react-router-dom";
import "./DoctorDashboard.css";
import ScrollReveal from "../../components/ui/ScrollReveal";

const Icon = ({ name, size = 20 }) => {
    const common = {
        width: size,
        height: size,
        viewBox: "0 0 24 24",
        fill: "none",
        xmlns: "http://www.w3.org/2000/svg",
        "aria-hidden": true
    };

    const paths = {
        dashboard: <><rect x="4" y="4" width="6" height="6" rx="1" /><rect x="14" y="4" width="6" height="6" rx="1" /><rect x="4" y="14" width="6" height="6" rx="1" /><rect x="14" y="14" width="6" height="6" rx="1" /></>,
        prescription: <><path d="M6 3h8l5 5v13H6a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2Z" /><path d="M14 3v5h5M8 13h8M8 17h6" /></>,
        patients: <><circle cx="9" cy="8" r="3" /><circle cx="17" cy="9" r="2" /><path d="M3 20a6 6 0 0 1 12 0M15 14a5 5 0 0 1 6 5" /></>,
        chat: <><path d="M20 11.5a8 8 0 0 1-12.27 6.78L4 20l.88-3.73A8 8 0 1 1 20 11.5Z" /><path d="M8 12h.01M12 12h.01M16 12h.01" /></>,
        pharmacy: <><path d="M8 3h8v4H8zM6 7h12a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V9a2 2 0 0 1 2-2Z" /><path d="M12 11v6M9 14h6" /></>,
        calendar: <><rect x="3" y="5" width="18" height="16" rx="2" /><path d="M16 3v4M8 3v4M3 10h18M8 14h2M14 14h2" /></>,
        profile: <><circle cx="12" cy="8" r="3" /><path d="M5 20a7 7 0 0 1 14 0" /></>,
        settings: <><circle cx="12" cy="12" r="3" /><path d="M19 15a2 2 0 0 0 2-2v-2a2 2 0 0 0-2-2l-1-2a2 2 0 0 0-1-2l-2 1-2-1a2 2 0 0 0-2 2l-2 1-2-1a2 2 0 0 0-1 2v2a2 2 0 0 0 2 2l1 2-1 2a2 2 0 0 0 1 2l2-1 2 1a2 2 0 0 0 2-2l2-1 2 1a2 2 0 0 0 1-2Z" /></>
    };

    return <svg {...common} stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round">{paths[name]}</svg>;
};

const navItems = [
    { label: "Dashboard", to: "/doctor", icon: "dashboard" },
    { label: "Prescription", to: "/doctor/prescription", icon: "prescription" },
    { label: "My Patients", to: "/doctor/patients", icon: "patients" },
    { label: "Messages", to: "/doctor/chat", icon: "chat" },
    { label: "Appointments", to: "/doctor/appointments", icon: "calendar" }
];

const DoctorDashboard = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const [sidebarOpen, setSidebarOpen] = useState(false);
    const allAppointments = JSON.parse(localStorage.getItem("appointments")) || [];
    const doctor = JSON.parse(localStorage.getItem("currentUser")) || {};

    const appointments = allAppointments.filter(
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

    const isChildRoute = location.pathname !== "/doctor" && location.pathname !== "/doctor/";
    const pending = appointments.filter((appointment) => appointment.status === "pending");
    const confirmed = appointments.filter((appointment) => appointment.status === "confirmed");

    const updateStatus = (id, status) => {
        const updatedAppointments = allAppointments.map((appointment) =>
            appointment.id === id ? { ...appointment, status } : appointment
        );

        localStorage.setItem("appointments", JSON.stringify(updatedAppointments));
        window.location.reload();
    };

    const name = doctor?.name || "Doctor";
    const initials = name.replace(/^Dr\.\s*/i, "").charAt(0).toUpperCase();

    return (
        <div className={`doctor-portal-shell${sidebarOpen ? " sidebar-is-open" : ""}`}>
            <button
                className="dashboard-sidebar-toggle"
                type="button"
                aria-label={sidebarOpen ? "Close navigation" : "Open navigation"}
                aria-expanded={sidebarOpen}
                onClick={() => setSidebarOpen((isOpen) => !isOpen)}
            >
                <span></span><span></span><span></span>
            </button>
            <div className="dashboard-sidebar-overlay" aria-hidden="true" onClick={() => setSidebarOpen(false)}></div>
            <aside className="doctor-sidebar" aria-label="Doctor navigation">
                <Link to="/doctor" className="doctor-sidebar-brand">
                    <img src="/Medicurex.jpg" alt="MediCurex logo" />
                    <span>MediCurex</span>
                </Link>

                <p className="doctor-sidebar-label">Workspace</p>
                <nav className="doctor-sidebar-nav">
                    {navItems.map((item) => {
                        const isActive = item.to === "/doctor"
                            ? !isChildRoute
                            : location.pathname.startsWith(item.to);

                        return (
                            <Link
                                key={item.to}
                                to={item.to}
                                className={`doctor-sidebar-link${isActive ? " is-active" : ""}`}
                            >
                                <Icon name={item.icon} size={18} />
                                <span>{item.label}</span>
                            </Link>
                        );
                    })}
                </nav>

                <div className="doctor-sidebar-secondary">
                    <p className="doctor-sidebar-label">Personal</p>
                    <span className="doctor-sidebar-link doctor-sidebar-link--disabled" aria-disabled="true"><Icon name="profile" size={18} /><span>Profile</span></span>
                    <span className="doctor-sidebar-link doctor-sidebar-link--disabled" aria-disabled="true"><Icon name="settings" size={18} /><span>Settings</span></span>
                </div>

                <div className="doctor-sidebar-user">
                    <div className="doctor-sidebar-avatar">{initials}</div>
                    <div><strong>{name}</strong><span>Doctor</span></div>
                </div>
            </aside>

            <main className="doctor-dashboard">
                {!isChildRoute && (
                    <div className="doctor-main-content">
                        <header className="doctor-dashboard-header">
                            <div>
                                <p className="doctor-eyebrow">CLINICAL WORKSPACE</p>
                                <h1>Welcome, {name} <span aria-hidden="true"></span></h1>
                                <p className="doctor-subtitle">Here is your daily overview.</p>
                            </div>
                            <div className="doctor-header-mark" aria-hidden="true"><Icon name="doctor" size={24} /></div>
                        </header>

                        <ScrollReveal className="feature-grid">
                            <div className="feature-card feature-card--active feature-card--hero" onClick={() => navigate("/doctor/prescription")}>
                                <span className="feature-icon"><Icon name="prescription" size={25} /></span>
                                <div><p className="feature-kicker">PATIENT CARE</p><h2>Prescription</h2><p>Write prescriptions for patients.</p></div>
                                <span className="feature-action">Open <span aria-hidden="true">→</span></span>
                            </div>
                            <div className="feature-card" onClick={() => navigate("/doctor/patients")}>
                                <span className="feature-icon"><Icon name="patients" size={22} /></span>
                                <h2>My Patients</h2><p>View patient profiles.</p>
                                <span className="feature-action">Open <span aria-hidden="true">→</span></span>
                            </div>
                            <Link to="chat" className="feature-card">
                                <span className="feature-icon"><Icon name="chat" size={22} /></span>
                                <h2>Messages</h2><p>Patient interaction.</p>
                                <span className="feature-action">Open <span aria-hidden="true">→</span></span>
                            </Link>
                            <div className="feature-card feature-card--khaki">
                                <span className="feature-icon"><Icon name="pharmacy" size={22} /></span>
                                <h2>Pharmacy</h2><p>Order medicines online.</p>
                                <span className="feature-action">Explore <span aria-hidden="true">→</span></span>
                            </div>
                        </ScrollReveal>

                        <ScrollReveal className="stats">
                            <div className="card"><p className="stat-label">Total Appointments</p><strong>{appointments.length}</strong></div>
                            <div className="card"><p className="stat-label">Pending</p><strong>{pending.length}</strong></div>
                            <div className="card"><p className="stat-label">Confirmed</p><strong>{confirmed.length}</strong></div>
                        </ScrollReveal>

                        <ScrollReveal className="table-card">
                            <div className="table-heading"><div><p className="doctor-eyebrow">TODAY / OVERVIEW</p><h2>Recent Appointments</h2></div><Icon name="calendar" size={25} /></div>
                            {appointments.length === 0 ? (
                                <div className="doctor-empty-state"><Icon name="calendar" size={28} /><p>No appointments yet</p></div>
                            ) : (
                                <div className="table-wrapper"><table><thead><tr><th>Patient</th><th>Date</th><th>Time</th><th>Status</th><th>Action</th></tr></thead><tbody>
                                    {appointments.map((appointment) => (
                                        <tr key={appointment.id}>
                                            <td>{appointment.patientName}</td><td>{appointment.date}</td><td>{appointment.time}</td>
                                            <td><span className={`status ${appointment.status}`}>{appointment.status}</span></td>
                                            <td>{appointment.status === "pending" && <div className="action-buttons"><button className="btn accept" onClick={() => updateStatus(appointment.id, "confirmed")}>Accept</button><button className="btn reject" onClick={() => updateStatus(appointment.id, "cancelled")}>Reject</button></div>}</td>
                                        </tr>
                                    ))}
                                </tbody></table></div>
                            )}
                        </ScrollReveal>
                    </div>
                )}

                {isChildRoute && <div className="doctor-child-content"><Outlet /></div>}
            </main>
        </div>
    );
};

export default DoctorDashboard;
