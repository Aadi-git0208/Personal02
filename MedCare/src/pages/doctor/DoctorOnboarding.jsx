import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "./DoctorDashboard.css";

const DoctorOnboarding = () => {
  const navigate = useNavigate();
  const doctor = JSON.parse(localStorage.getItem("currentUser"));

  const [specialization, setSpecialization] = useState("");
  const [experience, setExperience] = useState("");
  const [fee, setFee] = useState("");
  const [timeSlots, setTimeSlots] = useState([]);
  const [availableSlots, setAvailableSlots] = useState([
    "10:00 AM",
    "11:00 AM",
    "12:00 PM",
    "02:00 PM",
    "04:00 PM",
  ]);
  const [selectedSlot, setSelectedSlot] = useState("");

  useEffect(() => {
    if (!doctor || doctor.role !== "doctor") {
      navigate("/login");
    }
  }, [doctor, navigate]);

  const addTimeSlot = () => {
    if (selectedSlot && !timeSlots.includes(selectedSlot)) {
      setTimeSlots([...timeSlots, selectedSlot]);
      setSelectedSlot("");
    }
  };

  const removeTimeSlot = (slot) => {
    setTimeSlots(timeSlots.filter((s) => s !== slot));
  };

  const handleSubmit = (e) => {
    e.preventDefault();

    if (!specialization || !experience || !fee || timeSlots.length === 0) {
      alert("Please fill all required fields");
      return;
    }

    // Create doctor profile
    const doctorProfile = {
      id: doctor.id,
      userId: doctor.id,
      name: doctor.name,
      email: doctor.email,
      specialization: specialization,
      experience: experience,
      fee: Number(fee),
      timeSlots: timeSlots,
      image: doctor.image || "",
      createdAt: new Date().toISOString(),
      profileCompleted: true,
    };

    // Get existing doctor profiles
    const doctorProfiles =
      JSON.parse(localStorage.getItem("doctorProfiles")) || [];

    // Check if profile already exists
    const existingIndex = doctorProfiles.findIndex(
      (p) => p.id === doctor.id || p.userId === doctor.id
    );

    if (existingIndex >= 0) {
      doctorProfiles[existingIndex] = doctorProfile;
    } else {
      doctorProfiles.push(doctorProfile);
    }

    localStorage.setItem("doctorProfiles", JSON.stringify(doctorProfiles));

    // Mark doctor as having completed profile
    const users = JSON.parse(localStorage.getItem("users")) || [];
    const updatedUsers = users.map((user) =>
      user.id === doctor.id
        ? { ...user, profileCompleted: true }
        : user
    );
    localStorage.setItem("users", JSON.stringify(updatedUsers));

    // Update current user
    const updatedCurrentUser = {
      ...doctor,
      profileCompleted: true,
    };
    localStorage.setItem("currentUser", JSON.stringify(updatedCurrentUser));

    alert("Profile completed successfully! Redirecting to dashboard...");
    navigate("/doctor");
  };

  return (
    <div className="doctor-dashboard no-sidebar">
      <main className="main-content">
        <h1>Complete Your Doctor Profile</h1>
        <p className="subtitle">
          Please provide the following information to create your public profile
        </p>

        <form onSubmit={handleSubmit} className="onboarding-form">
          <div className="form-group">
            <label htmlFor="specialization">Specialization *</label>
            <input
              id="specialization"
              type="text"
              placeholder="e.g., Cardiologist, Dentist, Surgeon"
              value={specialization}
              onChange={(e) => setSpecialization(e.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="experience">Years of Experience *</label>
            <input
              id="experience"
              type="text"
              placeholder="e.g., 5 Years, 10 Years"
              value={experience}
              onChange={(e) => setExperience(e.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="fee">Consultation Fee (₹) *</label>
            <input
              id="fee"
              type="number"
              placeholder="e.g., 500"
              value={fee}
              onChange={(e) => setFee(e.target.value)}
              min="0"
              required
            />
          </div>

          <div className="form-group">
            <label>Available Time Slots *</label>
            <div className="time-slot-selector">
              <select
                value={selectedSlot}
                onChange={(e) => setSelectedSlot(e.target.value)}
              >
                <option value="">Select a time slot</option>
                {availableSlots
                  .filter((slot) => !timeSlots.includes(slot))
                  .map((slot) => (
                    <option key={slot} value={slot}>
                      {slot}
                    </option>
                  ))}
              </select>
              <button
                type="button"
                onClick={addTimeSlot}
                className="btn-add-slot"
              >
                Add Slot
              </button>
            </div>

            {timeSlots.length > 0 && (
              <div className="selected-slots">
                {timeSlots.map((slot) => (
                  <span key={slot} className="slot-tag">
                    {slot}
                    <button
                      type="button"
                      onClick={() => removeTimeSlot(slot)}
                      className="slot-remove"
                    >
                      ×
                    </button>
                  </span>
                ))}
              </div>
            )}
          </div>

          <button type="submit" className="btn-submit">
            Complete Profile
          </button>
        </form>
      </main>
    </div>
  );
};

export default DoctorOnboarding;

