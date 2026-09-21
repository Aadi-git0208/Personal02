import React from "react";
import { Link } from "react-router-dom";
import "./Admin.css"

const AdminDashboard = () => {
  const user = JSON.parse(localStorage.getItem("currentUser"));
  if (!user || user.role !== "admin") return <p>Access Denied</p>;

  const name = user.name || "Administrator";

  const users = JSON.parse(localStorage.getItem("users")) || [];
  const medicines = JSON.parse(localStorage.getItem("medicines")) || [];
  const orders = JSON.parse(localStorage.getItem("cart")) || [];

 return (
  <div className="admin-page">
    <header className="admin-dashboard-header">
      <div>
        <p className="admin-eyebrow">CONTROL CENTER</p>
        <h1>{name} <span aria-hidden="true"></span></h1>
        <p className="admin-subtitle">Keep your MediCurex platform organized and running smoothly.</p>
      </div>
      <div className="admin-header-mark" aria-hidden="true">✦</div>
    </header>

    <div className="admin-stats">
      <div className="admin-stat-card">
        <span className="admin-stat-kicker">PEOPLE</span>
        <h3>Total Users</h3>
        <p>{users.length}</p>
      </div>

      <div className="admin-stat-card">
        <span className="admin-stat-kicker">CATALOG</span>
        <h3>Total Medicines</h3>
        <p>{medicines.length}</p>
      </div>

      <div className="admin-stat-card">
        <span className="admin-stat-kicker">FULFILLMENT</span>
        <h3>Total Orders</h3>
        <p>{orders.length}</p>
      </div>
    </div>
    <div className="admin-links">
      <Link className="admin-link-card" to="/admin/users">
        <span className="admin-link-index">01</span>
        <span className="admin-link-copy"><strong>Manage Users</strong><small>Review patient and doctor accounts</small></span>
        <span className="admin-link-arrow" aria-hidden="true">→</span>
      </Link>

      <Link className="admin-link-card" to="/admin/medicines">
        <span className="admin-link-index">02</span><span className="admin-link-copy"><strong>Manage Medicines</strong><small>Maintain the medicine catalog</small></span><span className="admin-link-arrow" aria-hidden="true">→</span>
      </Link>

      <Link className="admin-link-card" to="/admin/orders">
        <span className="admin-link-index">03</span><span className="admin-link-copy"><strong>Orders</strong><small>Monitor pharmacy fulfillment</small></span><span className="admin-link-arrow" aria-hidden="true">→</span>
      </Link>

      <Link className="admin-link-card" to="/admin/reports">
        <span className="admin-link-index">04</span><span className="admin-link-copy"><strong>Reports</strong><small>Review platform activity</small></span><span className="admin-link-arrow" aria-hidden="true">→</span>
      </Link>
    </div>
  </div>
);

};

export default AdminDashboard;
