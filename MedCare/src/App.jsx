import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";

import Home from "./pages/Home";
import Navbar from "./components/Navbar";
import Login from "./pages/Login";
import Register from "./pages/Register";
import BookAppointment from "./pages/patient/BookAppointment";
import MyAppointment from "./pages/patient/MyAppointment";
import DoctorAppointments from "./pages/doctor/DoctorAppointments";

import "./App.css";

function App() {
  return (
    <div className="app">
      <Router>
        {/* Navbar visible on all pages */}
        <Navbar />
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/patient/book-appointment" element={<BookAppointment />} />
           <Route path="/patient/my-appointment" element={<MyAppointment />} />
          <Route path="/doctor/appointments" element={<DoctorAppointments />} />

        </Routes>
      </Router>
    </div>
  );
}

export default App;
