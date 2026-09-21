import React, { useState } from "react";
import { Outlet, Navigate, Link, useLocation } from "react-router-dom";
import "./Admin.css";

const AdminIcon = ({ name }) => {
  const paths = {
    dashboard: <><rect x="4" y="4" width="6" height="6" rx="1" /><rect x="14" y="4" width="6" height="6" rx="1" /><rect x="4" y="14" width="6" height="6" rx="1" /><rect x="14" y="14" width="6" height="6" rx="1" /></>,
    users: <><circle cx="9" cy="8" r="3" /><circle cx="17" cy="9" r="2" /><path d="M3 20a6 6 0 0 1 12 0M15 14a5 5 0 0 1 6 5" /></>,
    medicines: <><path d="M8 3h8v4H8zM6 7h12a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V9a2 2 0 0 1 2-2Z" /><path d="M12 11v6M9 14h6" /></>,
    orders: <><path d="M4 6h16v14H4zM8 3v6M16 3v6M4 11h16" /></>,
    reports: <><path d="M5 20V10M12 20V4M19 20v-7" /><path d="M3 20h18" /></>,
    settings: <><circle cx="12" cy="12" r="3" /><path d="M19 15a2 2 0 0 0 2-2v-2a2 2 0 0 0-2-2l-1-2a2 2 0 0 0-1-2l-2 1-2-1a2 2 0 0 0-2 2l-2 1-2-1a2 2 0 0 0-1 2v2a2 2 0 0 0 2 2l1 2-1 2a2 2 0 0 0 1 2l2-1 2 1a2 2 0 0 0 2-2l2-1 2 1a2 2 0 0 0 1-2Z" /></>
  };

  return <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">{paths[name]}</svg>;
};

const AdminLayout = () => {
  const user = JSON.parse(localStorage.getItem("currentUser"));
  const location = useLocation();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  if (!user || user.role !== "admin") {
    return <Navigate to="/login" replace />;
  }

  const navItems = [
    ["Dashboard", "/admin/dashboard", "dashboard"],
    ["Manage Users", "/admin/users", "users"],
    ["Manage Medicines", "/admin/medicines", "medicines"],
    ["Orders", "/admin/orders", "orders"],
    ["Reports", "/admin/reports", "reports"]
  ];

  const initials = (user.name || "Admin").charAt(0).toUpperCase();

  return (
    <div className={`admin-shell${sidebarOpen ? " sidebar-is-open" : ""}`}>
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
      <aside className="admin-sidebar" aria-label="Admin navigation">
        <Link to="/admin/dashboard" className="admin-sidebar-brand">
          <img src="/Medicurex.jpg" alt="MediCurex logo" />
          <span>MediCurex</span>
        </Link>
        <p className="admin-sidebar-label">Workspace</p>
        <nav className="admin-sidebar-nav">
          {navItems.map(([label, to, icon]) => (
            <Link key={to} to={to} className={`admin-sidebar-link${location.pathname === to || (to === "/admin/dashboard" && location.pathname === "/admin") ? " is-active" : ""}`}>
              <AdminIcon name={icon} />
              <span>{label}</span>
            </Link>
          ))}
        </nav>
        <div className="admin-sidebar-secondary">
          <p className="admin-sidebar-label">System</p>
          <span className="admin-sidebar-link admin-sidebar-link--disabled" aria-disabled="true"><AdminIcon name="settings" /><span>Settings</span></span>
        </div>
        <div className="admin-sidebar-user">
          <div className="admin-sidebar-avatar">{initials}</div>
          <div><strong>{user.name || "Administrator"}</strong><span>Administrator</span></div>
        </div>
      </aside>
      <main className="admin-main"><Outlet /></main>
    </div>
  );
};

export default AdminLayout;
