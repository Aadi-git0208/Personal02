import React, { useState } from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";

import Navbar from "./components/Navbar";
import { GuestRoute, ProtectedRoute } from "./components/ProtectedRoute";
import { AuthProvider } from "./auth/AuthContext";

import Home from "./pages/Home";
import Login from "./pages/Login";
import Register from "./pages/Register";

import PatientDashboard from "./pages/patient/PatientDashboard";
import BookAppointment from "./pages/patient/BookAppointment";
import MyAppointment from "./pages/patient/MyAppointment";
import ChatWithDoctor from "./pages/patient/ChatWithDoctor";
import Pharmacy from "./pages/patient/Pharmacy";
import Prescription from "./pages/patient/Prescription";

import DoctorDashboard from "./pages/doctor/DoctorDashboard";
import DoctorAppointments from "./pages/doctor/DoctorAppointments";
import DoctorChat from "./pages/doctor/DoctorChat";
import PatientList from "./pages/doctor/PatientList";
import WritePrescription from "./pages/doctor/WritePrescription";
import DoctorLogin from "./pages/doctor/DoctorLogin";
import DoctorOnboarding from "./pages/doctor/DoctorOnboarding";

import MedicineDashboard from "./components/MedicineDashboard";

import AdminLayout from "./pages/admin/AdminLayout";
import AdminDashboard from "./pages/admin/AdminDashboard";
import ManageUsers from "./pages/admin/ManageUsers";
import Orders from "./pages/admin/Orders";
import Reports from "./pages/admin/Reports";
import ManageMedicines from "./pages/admin/ManageMedicines";

import Cart from "./components/Cart";

import Services from "./components/Service/ServiceSection.jsx";
import "./components/Service/leafletFix.js";
import NearbyHospitals from "./components/Service/NearbyHospitals.jsx";

import "./App.css";

function App() {
    const [darkMode, setDarkMode] = useState(true);

    return (
        <AuthProvider>
        <Router>
            <div className={darkMode ? "dark-mode" : "light-mode"}>
                <Navbar
                    darkMode={darkMode}
                    setDarkMode={setDarkMode}
                />

                <Routes>
                    <Route path="/" element={<Home />} />

                    <Route
                        path="/login"
                        element={(
                            <GuestRoute>
                                <Login />
                            </GuestRoute>
                        )}
                    />

                    <Route
                        path="/register"
                        element={(
                            <GuestRoute>
                                <Register />
                            </GuestRoute>
                        )}
                    />

                    <Route
                        path="/medicine"
                        element={<MedicineDashboard />}
                    />

                    <Route
                        path="/cart"
                        element={<Cart />}
                    />

                    <Route
                        path="/services"
                        element={<Services />}
                    />

                    <Route
                        path="/nearby-hospitals"
                        element={<NearbyHospitals />}
                    />

                    <Route
                        path="/patient"
                        element={(
                            <ProtectedRoute roles={["patient"]}>
                                <PatientDashboard />
                            </ProtectedRoute>
                        )}
                    >
                        <Route
                            index
                            element={<BookAppointment />}
                        />

                        <Route
                            path="dashboard"
                            element={<BookAppointment />}
                        />

                        <Route
                            path="book-appointment"
                            element={<BookAppointment />}
                        />

                        <Route
                            path="my-appointment"
                            element={<MyAppointment />}
                        />

                        <Route
                            path="chat"
                            element={<ChatWithDoctor />}
                        />

                        <Route
                            path="pharmacy"
                            element={<Pharmacy />}
                        />

                        <Route
                            path="prescription"
                            element={<Prescription />}
                        />
                    </Route>

                    <Route
                        path="/doctor"
                        element={(
                            <ProtectedRoute roles={["doctor"]}>
                                <DoctorDashboard />
                            </ProtectedRoute>
                        )}
                    >
                        <Route
                            index
                            element={<DoctorAppointments />}
                        />

                        <Route
                            path="appointments"
                            element={<DoctorAppointments />}
                        />

                        <Route
                            path="chat"
                            element={<DoctorChat />}
                        />

                        <Route
                            path="patients"
                            element={<PatientList />}
                        />

                        <Route
                            path="prescription"
                            element={<WritePrescription />}
                        />

                        <Route
                            path="login"
                            element={<DoctorLogin />}
                        />
                    </Route>

                    <Route
                        path="/doctor/onboarding"
                        element={(
                            <ProtectedRoute roles={["doctor"]}>
                                <DoctorOnboarding />
                            </ProtectedRoute>
                        )}
                    />

                    <Route
                        path="/admin"
                        element={(
                            <ProtectedRoute roles={["admin"]}>
                                <AdminLayout />
                            </ProtectedRoute>
                        )}
                    >
                        <Route
                            index
                            element={<AdminDashboard />}
                        />

                        <Route
                            path="dashboard"
                            element={<AdminDashboard />}
                        />

                        <Route
                            path="users"
                            element={<ManageUsers />}
                        />

                        <Route
                            path="medicines"
                            element={<ManageMedicines />}
                        />

                        <Route
                            path="orders"
                            element={<Orders />}
                        />

                        <Route
                            path="reports"
                            element={<Reports />}
                        />
                    </Route>
                </Routes>
            </div>
        </Router>
        </AuthProvider>
    );
}

export default App;