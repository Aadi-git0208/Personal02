import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";

import Navbar from "./components/Navbar";

import Home from "./pages/Home";
import Login from "./pages/Login";
import Register from "./pages/Register";



// Patient
import PatientDashboard from "./pages/patient/PatientDashboard";
import BookAppointment from "./pages/patient/BookAppointment";
import MyAppointment from "./pages/patient/MyAppointment";
import ChatWithDoctor from "./pages/patient/ChatWithDoctor";
import Pharmacy from "./pages/patient/Pharmacy";
import Prescription from "./pages/patient/Prescription";

// Doctor
import DoctorDashboard from "./pages/doctor/DoctorDashboard";
import DoctorAppointments from "./pages/doctor/DoctorAppointments";
import DoctorChat from "./pages/doctor/DoctorChat";
import PatientList from "./pages/doctor/PatientList";
import WritePrescription from "./pages/doctor/WritePrescription";
import DoctorLogin from "./pages/doctor/DoctorLogin";
import DoctorOnboarding from "./pages/doctor/DoctorOnboarding";
import MedicineDashboard from "./components/MedicineDashboard";
//Admin
import AdminLayout from "./pages/admin/AdminLayout";
import AdminDashboard from "./pages/admin/AdminDashboard";
// import ManageDoctor from "./pages/admin/ManageDoctor";
import ManageUsers from "./pages/admin/ManageUsers";
import Orders from "./pages/admin/Orders";
import Reports from "./pages/admin/Reports";
import ManageMedicines from "./pages/admin/ManageMedicines";
import Cart from "./components/Cart"; 
import Services from "./components/Service/ServiceSection.jsx";
import "./components/Service/leafletFix.js";
import NearbyHospitals from "./components/Service/NearbyHospitals.jsx";



function App() {
  return (
    <Router>
      {/* ✅ NAVBAR VISIBLE ON ALL PAGES */}
      <Navbar />

      <Routes>
        {/* ===== PUBLIC ===== */}
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
          <Route path="/medicine" element={<MedicineDashboard />} />
          <Route path="/cart" element={<Cart />} />
          <Route path="/services" element={<Services />} />
          <Route path="/nearby-hospitals" element={<NearbyHospitals />} />

       {/* ===== PATIENT ===== */}
<Route path="/patient" element={<PatientDashboard />}>
  <Route index element={<BookAppointment />} />
  <Route path="dashboard" element={<BookAppointment />} />
 
  <Route path="book-appointment" element={<BookAppointment />} />
  <Route path="my-appointment" element={<MyAppointment />} />
  <Route path="chat" element={<ChatWithDoctor />} />
  <Route path="pharmacy" element={<Pharmacy />} />
  <Route path="prescription" element={<Prescription />} />
</Route>


        {/* ===== DOCTOR ===== */}
        <Route path="/doctor" element={<DoctorDashboard />}>
          <Route index element={<DoctorAppointments />} />
          <Route path="appointments" element={<DoctorAppointments />} />
          <Route path="chat" element={<DoctorChat />} />
          <Route path="patients" element={<PatientList />} />
          <Route path="prescription" element={<WritePrescription />} />
          <Route path="login" element={<DoctorLogin />} />
        </Route>
        <Route path="/doctor/onboarding" element={<DoctorOnboarding />} />
      <Route path="/admin" element={<AdminLayout />}>
  <Route index element={<AdminDashboard />} />
  <Route path="dashboard" element={<AdminDashboard />} />
  <Route path="users" element={<ManageUsers />} />
  {/* <Route path="doctors" element={<ManageDoctor />} /> */}
  <Route path="medicines" element={<ManageMedicines />} />
  <Route path="orders" element={<Orders />} />
  <Route path="reports" element={<Reports />} />
</Route>


      </Routes>
    </Router>
  );
}

export default App;
