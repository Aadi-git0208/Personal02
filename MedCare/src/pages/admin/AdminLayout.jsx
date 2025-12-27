import React from "react";
import { Outlet, Navigate } from "react-router-dom";

const AdminLayout = () => {
  const user = JSON.parse(localStorage.getItem("currentUser"));

  if (!user || user.role !== "admin") {
    return <Navigate to="/login" replace />;
  }

  return (
    <div style={{ padding: "20px" }}>
      {/* Admin pages will render here */}
      <Outlet />
    </div>
  );
};

export default AdminLayout;
