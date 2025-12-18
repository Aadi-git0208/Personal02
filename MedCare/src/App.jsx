import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";

import Home from "./pages/Home";
import Navbar from "./components/Navbar";
import Login from "./pages/Login";
import Register from "./pages/Register";

import PatientDashboard from "./pages/patient/PatientDashboard";
import BookAppointment from "./pages/patient/BookAppointment";
import MyAppointment from "./pages/patient/MyAppointment";
import ChatWithDoctor from "./pages/patient/ChatWithDoctor";
// import Pharmacy from "./pages/patient/Pharmacy";
// import Prescription from "./pages/patient/Prescription";

import DoctorAppointments from "./pages/doctor/DoctorAppointments";

function App() {
  return (
    <Router>
      <Routes>
        {/* ===== PUBLIC PAGES (WITH MAIN NAVBAR) ===== */}
        <Route
          path="/"
          element={
            <>
              <Navbar />
              <Home />
            </>
          }
        />

        <Route
          path="/login"
          element={
            <>
              <Navbar />
              <Login />
            </>
          }
        />

        <Route
          path="/register"
          element={
            <>
              <Navbar />
              <Register />
            </>
          }
        />

        {/* ===== PATIENT PORTAL (NO MAIN NAVBAR) ===== */}
        <Route path="/patient" element={<PatientDashboard />}>
          <Route index element={<BookAppointment />} />
          <Route path="dashboard" element={<BookAppointment />} />
          <Route path="book-appointment" element={<BookAppointment />} />
          <Route path="my-appointment" element={<MyAppointment />} />
          <Route path="chat" element={<ChatWithDoctor />} />
          {/* <Route path="pharmacy" element={<Pharmacy />} />
          <Route path="prescription" element={<Prescription />} /> */}
        </Route>

        {/* ===== DOCTOR ===== */}
        <Route path="/doctor/appointments" element={<DoctorAppointments />} />
      </Routes>
    </Router>
  );
}

export default App;
