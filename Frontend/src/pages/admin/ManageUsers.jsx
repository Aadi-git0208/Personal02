import React, { useState } from "react";
import "./Admin.css";

const ManageUsers = () => {
  const [users, setUsers] = useState(
    JSON.parse(localStorage.getItem("users")) || []
  );

  // New user form state
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [role, setRole] = useState("patient");

  /* ===== DELETE USER ===== */
  const deleteUser = (email) => {
    const updated = users.filter((u) => u.email !== email);
    localStorage.setItem("users", JSON.stringify(updated));
    setUsers(updated);
    alert("User removed");
  };

  /* ===== ADD USER ===== */
  const addUser = () => {
    if (!name || !email || !password) {
      alert("Please fill all fields");
      return;
    }

    const exists = users.find((u) => u.email === email);
    if (exists) {
      alert("User already exists");
      return;
    }

    const newUser = { name, email, password, role };
    const updatedUsers = [...users, newUser];

    localStorage.setItem("users", JSON.stringify(updatedUsers));
    setUsers(updatedUsers);

    // Reset form
    setName("");
    setEmail("");
    setPassword("");
    setRole("patient");

    alert("User added successfully ✅");
  };

  return (
    <div className="admin-page">
      <h1>Manage Users</h1>

      {/* ===== ADD USER SECTION ===== */}
      <div className="admin-section">
        <h2>Add New User</h2>

        <input
          className="admin-input"
          type="text"
          placeholder="Name"
          value={name}
          onChange={(e) => setName(e.target.value)}
        />

        <input
          className="admin-input"
          type="email"
          placeholder="Email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />

        <input
          className="admin-input"
          type="password"
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />

        <select
          className="admin-input"
          value={role}
          onChange={(e) => setRole(e.target.value)}
        >
          <option value="patient">Patient</option>
          <option value="doctor">Doctor</option>
          <option value="admin">Admin</option>
        </select>

        <button className="admin-btn" onClick={addUser}>
          Add User
        </button>
      </div>

      {/* ===== USER LIST ===== */}
      <div className="admin-section">
        <h2>User List</h2>

        {/* HEADER */}
        <div className="admin-row admin-header">
          <span>Name</span>
          <span>Email</span>
          <span>Profession</span>
          <span>Action</span>
        </div>

        {users.length === 0 ? (
          <p className="admin-empty">No users available</p>
        ) : (
          users.map((u) => (
            <div className="admin-row" key={u.email}>
              <span>{u.name}</span>
              <span>{u.email}</span>
              <span className={`role ${u.role}`}>{u.role}</span>
              <button
                className="admin-btn admin-btn-danger"
                onClick={() => deleteUser(u.email)}
              >
                Delete
              </button>
            </div>
          ))
        )}
      </div>
    </div>
  );
};

export default ManageUsers;
