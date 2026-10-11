import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { ApiError, getApiErrorMessage } from "../../api/client";
import { getDoctorProfile, saveDoctorProfile } from "../../api/profileApi";
import { useAuth } from "../../auth/AuthContext";
import "./DoctorDashboard.css";

const availableSlots = [
    "10:00 AM",
    "11:00 AM",
    "12:00 PM",
    "02:00 PM",
    "04:00 PM"
];

const DoctorOnboarding = () => {
    const navigate = useNavigate();
    const { user, refreshUser } = useAuth();

    const [fullName, setFullName] = useState(user?.name || "");
    const [specialization, setSpecialization] = useState("");
    const [qualifications, setQualifications] = useState("");
    const [experience, setExperience] = useState("");
    const [fee, setFee] = useState("");
    const [phoneNumber, setPhoneNumber] = useState("");
    const [clinicAddress, setClinicAddress] = useState("");
    const [biography, setBiography] = useState("");
    const [timeSlots, setTimeSlots] = useState([]);
    const [selectedSlot, setSelectedSlot] = useState("");
    const [approvalStatus, setApprovalStatus] = useState(user?.approvalStatus || null);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState("");

    useEffect(() => {
        let cancelled = false;

        async function loadProfile() {
            setLoading(true);
            try {
                const profile = await getDoctorProfile();
                if (cancelled || !profile) {
                    return;
                }
                setFullName(profile.fullName || user?.name || "");
                setSpecialization(profile.specialization || "");
                setQualifications(profile.qualifications || "");
                setExperience(profile.experienceYears != null ? String(profile.experienceYears) : "");
                setFee(profile.consultationFee != null ? String(profile.consultationFee) : "");
                setPhoneNumber(profile.phoneNumber || "");
                setClinicAddress(profile.clinicAddress || "");
                setBiography(profile.biography || "");
                setTimeSlots(profile.availabilitySlots || []);
                setApprovalStatus(profile.approvalStatus || null);
            } catch (loadError) {
                if (!(loadError instanceof ApiError && loadError.status === 404) && !cancelled) {
                    setError(getApiErrorMessage(loadError));
                }
            } finally {
                if (!cancelled) {
                    setLoading(false);
                }
            }
        }

        loadProfile();
        return () => {
            cancelled = true;
        };
    }, [user?.name]);

    const addTimeSlot = () => {
        if (selectedSlot && !timeSlots.includes(selectedSlot)) {
            setTimeSlots([...timeSlots, selectedSlot]);
            setSelectedSlot("");
        }
    };

    const removeTimeSlot = (slot) => {
        setTimeSlots(timeSlots.filter((item) => item !== slot));
    };

    const handleSubmit = async (event) => {
        event.preventDefault();
        setError("");

        if (
            !fullName.trim() ||
            !specialization.trim() ||
            !qualifications.trim() ||
            experience === "" ||
            fee === "" ||
            !phoneNumber.trim() ||
            !clinicAddress.trim() ||
            timeSlots.length === 0
        ) {
            setError("Please fill all required fields, including at least one time slot.");
            return;
        }

        setSaving(true);
        try {
            const saved = await saveDoctorProfile({
                fullName: fullName.trim(),
                specialization: specialization.trim(),
                qualifications: qualifications.trim(),
                experienceYears: Number(experience),
                consultationFee: Number(fee),
                clinicAddress: clinicAddress.trim(),
                phoneNumber: phoneNumber.trim(),
                biography: biography.trim(),
                availabilitySlots: timeSlots
            });
            setApprovalStatus(saved.approvalStatus);
            await refreshUser();
            navigate("/doctor");
        } catch (saveError) {
            setError(getApiErrorMessage(saveError));
        } finally {
            setSaving(false);
        }
    };

    return (
        <div className="doctor-dashboard no-sidebar">
            <main className="main-content">
                <h1>{user?.profileCompleted ? "Update Your Doctor Profile" : "Complete Your Doctor Profile"}</h1>
                <p className="subtitle">
                    Please provide the following information to create your public profile
                </p>
                {approvalStatus === "PENDING" && (
                    <p>Your profile is waiting for administrator approval before it appears in the public directory.</p>
                )}
                {approvalStatus === "REJECTED" && (
                    <p role="alert">Your profile was not approved. Update your details and wait for another review.</p>
                )}
                {error && <p role="alert">{error}</p>}
                {loading ? (
                    <p>Loading profile...</p>
                ) : (
                    <form onSubmit={handleSubmit} className="onboarding-form">
                        <div className="form-group">
                            <label htmlFor="fullName">Full name *</label>
                            <input
                                id="fullName"
                                type="text"
                                value={fullName}
                                onChange={(event) => setFullName(event.target.value)}
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="specialization">Specialization *</label>
                            <input
                                id="specialization"
                                type="text"
                                placeholder="e.g., Cardiologist, Dentist, Surgeon"
                                value={specialization}
                                onChange={(event) => setSpecialization(event.target.value)}
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="qualifications">Qualifications *</label>
                            <input
                                id="qualifications"
                                type="text"
                                placeholder="e.g., MBBS, MD"
                                value={qualifications}
                                onChange={(event) => setQualifications(event.target.value)}
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="experience">Years of Experience *</label>
                            <input
                                id="experience"
                                type="number"
                                min="0"
                                max="70"
                                placeholder="e.g., 5"
                                value={experience}
                                onChange={(event) => setExperience(event.target.value)}
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
                                onChange={(event) => setFee(event.target.value)}
                                min="0"
                                step="0.01"
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="phoneNumber">Phone number *</label>
                            <input
                                id="phoneNumber"
                                type="tel"
                                placeholder="10 to 15 digits"
                                value={phoneNumber}
                                onChange={(event) => setPhoneNumber(event.target.value)}
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="clinicAddress">Clinic or hospital address *</label>
                            <input
                                id="clinicAddress"
                                type="text"
                                value={clinicAddress}
                                onChange={(event) => setClinicAddress(event.target.value)}
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="biography">Biography</label>
                            <textarea
                                id="biography"
                                value={biography}
                                onChange={(event) => setBiography(event.target.value)}
                            />
                        </div>
                        <div className="form-group">
                            <label>Available Time Slots *</label>
                            <div className="time-slot-selector">
                                <select
                                    value={selectedSlot}
                                    onChange={(event) => setSelectedSlot(event.target.value)}
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
                                <button type="button" onClick={addTimeSlot} className="btn-add-slot">
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
                        <button type="submit" className="btn-submit" disabled={saving}>
                            {saving ? "Saving..." : user?.profileCompleted ? "Save Profile" : "Complete Profile"}
                        </button>
                    </form>
                )}
            </main>
        </div>
    );
};

export default DoctorOnboarding;
