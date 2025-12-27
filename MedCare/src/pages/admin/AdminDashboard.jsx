import React from "react";
import { Link } from "react-router-dom";
import "./Admin.css"

const AdminDashboard = () => {
  const user = JSON.parse(localStorage.getItem("currentUser"));
  if (!user || user.role !== "admin") return <p>Access Denied</p>;

  const users = JSON.parse(localStorage.getItem("users")) || [];
  const medicines = JSON.parse(localStorage.getItem("medicines")) || [];
  const orders = JSON.parse(localStorage.getItem("cart")) || [];

 return (
  <div className="admin-page">
    <h1>Admin Dashboard</h1>

    <div className="admin-stats">
      <div className="admin-stat-card">
        <h3>Total Users</h3>
        <p>{users.length}</p>
      </div>

      <div className="admin-stat-card">
        <h3>Total Medicines</h3>
        <p>{medicines.length}</p>
      </div>

      <div className="admin-stat-card">
        <h3>Total Orders</h3>
        <p>{orders.length}</p>
      </div>
    </div>
    <div className="admin-links">
      <Link className="admin-link-card" to="/admin/users">
        Manage Users
      </Link>

      <Link className="admin-link-card" to="/admin/medicines">
        Manage Medicines
      </Link>

      <Link className="admin-link-card" to="/admin/orders">
        Orders
      </Link>

      <Link className="admin-link-card" to="/admin/reports">
        Reports
      </Link>
    </div>
  </div>
);

};

export default AdminDashboard;
