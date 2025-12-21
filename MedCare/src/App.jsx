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

// Doctor
import DoctorDashboard from "./pages/doctor/DoctorDashboard";
import DoctorAppointments from "./pages/doctor/DoctorAppointments";
import DoctorChat from "./pages/doctor/DoctorChat";
import PatientList from "./pages/doctor/PatientList";
import WritePrescription from "./pages/doctor/WritePrescription";
import DoctorLogin from "./pages/doctor/DoctorLogin";


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

        {/* ===== PATIENT ===== */}
        <Route path="/patient" element={<PatientDashboard />}>
          <Route index element={<BookAppointment />} />
          <Route path="dashboard" element={<BookAppointment />} />
          <Route path="book-appointment" element={<BookAppointment />} />
          <Route path="my-appointment" element={<MyAppointment />} />
          <Route path="chat" element={<ChatWithDoctor />} />
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
      </Routes>
    </Router>
  );
}

export default App;
