import { useState } from "react";
import { Link, Outlet, useLocation } from "react-router-dom";
import "./PatientDashboard.css";
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
        calendar: <><rect x="3" y="5" width="18" height="16" rx="2" /><path d="M16 3v4M8 3v4M3 10h18M8 14h2M14 14h2" /></>,
        doctor: <><path d="M12 3v4M9 5h6M5 12a7 7 0 0 0 14 0V9H5v3Z" /><path d="M12 16v5M9 21h6" /></>,
        chat: <><path d="M20 11.5a8 8 0 0 1-12.27 6.78L4 20l.88-3.73A8 8 0 1 1 20 11.5Z" /><path d="M8 12h.01M12 12h.01M16 12h.01" /></>,
        pharmacy: <><path d="M8 3h8v4H8zM6 7h12a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V9a2 2 0 0 1 2-2Z" /><path d="M12 11v6M9 14h6" /></>,
        prescription: <><path d="M6 3h8l5 5v13H6a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2Z" /><path d="M14 3v5h5M8 13h8M8 17h6" /></>,
        profile: <><circle cx="12" cy="8" r="3" /><path d="M5 20a7 7 0 0 1 14 0" /></>,
        settings: <><circle cx="12" cy="12" r="3" /><path d="M19.4 15a1.7 1.7 0 0 0 .34 1.88l.06.06-1.41 1.41-.06-.06a1.7 1.7 0 0 0-1.88-.34 1.7 1.7 0 0 0-1 1.55V20h-2v-.5a1.7 1.7 0 0 0-1-1.55 1.7 1.7 0 0 0-1.88.34l-.06.06-1.41-1.41.06-.06A1.7 1.7 0 0 0 9.5 15a1.7 1.7 0 0 0-1.55-1H7v-2h.5a1.7 1.7 0 0 0 1.55-1 1.7 1.7 0 0 0-.34-1.88l-.06-.06 1.41-1.41.06.06A1.7 1.7 0 0 0 12 8.5 1.7 1.7 0 0 0 13 7V6h2v.5a1.7 1.7 0 0 0 1 1.55 1.7 1.7 0 0 0 1.88-.34l.06-.06 1.41 1.41-.06.06A1.7 1.7 0 0 0 19.5 11c.15.6.7 1 1.5 1h.5v2H21a1.7 1.7 0 0 0-1.6 1Z" /></>
    };

    return <svg {...common} stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round">{paths[name]}</svg>;
};

const navItems = [
    { label: "Dashboard", to: "/patient", icon: "dashboard" },
    { label: "Book Appointment", to: "/patient/book-appointment", icon: "doctor" },
    { label: "My Appointments", to: "/patient/my-appointment", icon: "calendar" },
    { label: "Chat with Doctor", to: "/patient/chat", icon: "chat" },
    { label: "Pharmacy", to: "/patient/pharmacy", icon: "pharmacy" },
    { label: "Prescription", to: "/patient/prescription", icon: "prescription" }
];

function PatientDashboard() {
    const user = JSON.parse(localStorage.getItem("currentUser"));
    const location = useLocation();
    const [sidebarOpen, setSidebarOpen] = useState(false);
    const name = user?.name || "Patient";
    const initials = name.charAt(0).toUpperCase();

    return (
        <div className={`patient-portal-shell${sidebarOpen ? " sidebar-is-open" : ""}`}>
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
            <aside className="patient-sidebar" aria-label="Patient navigation">
                <Link to="/patient" className="patient-sidebar-brand">
                    <img src="/Medicurex.jpg" alt="MediCurex logo" />
                    <span>MediCurex</span>
                </Link>

                <p className="patient-sidebar-label">Workspace</p>
                <nav className="patient-sidebar-nav">
                    {navItems.map((item) => {
                        const isActive = item.to === "/patient"
                            ? location.pathname === "/patient" || location.pathname === "/patient/"
                            : location.pathname.startsWith(item.to);

                        return (
                            <Link
                                key={item.to}
                                to={item.to}
                                className={`patient-sidebar-link${isActive ? " is-active" : ""}`}
                            >
                                <Icon name={item.icon} size={18} />
                                <span>{item.label}</span>
                            </Link>
                        );
                    })}
                </nav>

                <div className="patient-sidebar-secondary">
                    <p className="patient-sidebar-label">Personal</p>
                    <span className="patient-sidebar-link patient-sidebar-link--disabled" aria-disabled="true">
                        <Icon name="profile" size={18} />
                        <span>Profile</span>
                    </span>
                    <span className="patient-sidebar-link patient-sidebar-link--disabled" aria-disabled="true">
                        <Icon name="settings" size={18} />
                        <span>Settings</span>
                    </span>
                </div>

                <div className="patient-sidebar-user">
                    <div className="patient-sidebar-avatar">{initials}</div>
                    <div>
                        <strong>{name}</strong>
                        <span>Patient</span>
                    </div>
                </div>
            </aside>

            <main className="patient-dashboard">
                <div className="dashboard-header">
                    <div className="dashboard-welcome">
                        <p className="dashboard-eyebrow">PATIENT PORTAL</p>
                        <h1>Good day, {name} <span aria-hidden="true"></span></h1>
                        <p className="dashboard-subtitle">Manage your healthcare, appointments and prescriptions in one place.</p>
                    </div>
                    <label className="dashboard-search">
                        <Icon name="doctor" size={18} />
                        <input type="search" placeholder="Search doctors, specialties..." aria-label="Search doctors and specialties" />
                    </label>
                </div>

                <ScrollReveal className="dashboard-boxes">
                    <Link to="/patient/book-appointment" className="dashboard-box dashboard-box--hero">
                        <span className="dashboard-icon"><Icon name="doctor" size={24} /></span>
                        <div>
                            <p className="dashboard-box-kicker">CARE, ON YOUR TERMS</p>
                            <h2>Book Appointment</h2>
                            <p>Find doctors and book appointments.</p>
                        </div>
                        <span className="dashboard-action">Open <span aria-hidden="true">→</span></span>
                        <span className="dashboard-botanical" aria-hidden="true">✦</span>
                    </Link>

                    <Link to="/patient/my-appointment" className="dashboard-box dashboard-box--appointments">
                        <span className="dashboard-icon"><Icon name="calendar" size={22} /></span>
                        <h2>My Appointments</h2>
                        <p>View appointment history.</p>
                        <span className="dashboard-action">Open <span aria-hidden="true">→</span></span>
                    </Link>

                    <Link to="/patient/chat" className="dashboard-box dashboard-box--chat">
                        <span className="dashboard-icon"><Icon name="chat" size={22} /></span>
                        <h2>Chat with Doctor</h2>
                        <p>Consult doctors online.</p>
                        <span className="dashboard-action">Open <span aria-hidden="true">→</span></span>
                    </Link>

                    <Link to="/patient/pharmacy" className="dashboard-box dashboard-box--pharmacy">
                        <span className="dashboard-icon"><Icon name="pharmacy" size={22} /></span>
                        <h2>Pharmacy</h2>
                        <p>Order medicines online.</p>
                        <span className="dashboard-action">Open <span aria-hidden="true">→</span></span>
                    </Link>

                    <Link to="/patient/prescription" className="dashboard-box dashboard-box--prescription">
                        <span className="dashboard-icon"><Icon name="prescription" size={22} /></span>
                        <h2>Prescription</h2>
                        <p>View doctor prescriptions.</p>
                        <span className="dashboard-action">Open <span aria-hidden="true">→</span></span>
                    </Link>
                </ScrollReveal>

                <section className="dashboard-content-section">
                    <div className="dashboard-section-heading">
                        <div>
                            <p className="dashboard-eyebrow">YOUR CARE</p>
                            <h2>Find a Doctor</h2>
                            <p>Find the right healthcare professional for your needs.</p>
                        </div>
                    </div>
                    <div className="dashboard-filter-chips" aria-label="Doctor specialty filters">
                        <span className="dashboard-chip is-active">All</span>
                        <span className="dashboard-chip">Specialties from your doctors</span>
                    </div>
                    <div className="dashboard-outlet">
                        <Outlet />
                    </div>
                </section>
            </main>
        </div>
    );
}

export default PatientDashboard;
