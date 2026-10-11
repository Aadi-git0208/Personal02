import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getApiErrorMessage } from "../../api/client";
import { getDoctorDirectory } from "../../api/profileApi";
import {
    createAppointment,
    getDoctorAvailability,
    parseSlotTimes
} from "../../api/appointmentApi";
import { useAuth } from "../../auth/AuthContext";
import "./BookAppointment.css";
import ScrollReveal from "../../components/ui/ScrollReveal";

function localIsoDate(offsetDays = 0) {
    const date = new Date();
    date.setDate(date.getDate() + offsetDays);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
}

function isSlotBooked(slotLabel, bookedRanges) {
    const times = parseSlotTimes(slotLabel);
    if (!times || !Array.isArray(bookedRanges)) {
        return false;
    }

    return bookedRanges.some((range) => {
        const start = String(range.startTime || "");
        return start === times.startTime || start.startsWith(times.startTime.slice(0, 5));
    });
}

function BookAppointment() {
    const navigate = useNavigate();
    const { user } = useAuth();

    const [doctors, setDoctors] = useState([]);
    const [selectedDoctor, setSelectedDoctor] = useState(null);
    const [appointmentDate, setAppointmentDate] = useState(() => localIsoDate(1));
    const [slot, setSlot] = useState("");
    const [reason, setReason] = useState("");
    const [notes, setNotes] = useState("");
    const [availability, setAvailability] = useState(null);
    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState("");

    useEffect(() => {
        let cancelled = false;

        async function loadDoctors() {
            setLoading(true);
            setError("");
            try {
                const directory = await getDoctorDirectory();
                if (!cancelled) {
                    setDoctors(Array.isArray(directory) ? directory : []);
                }
            } catch (loadError) {
                if (!cancelled) {
                    setError(getApiErrorMessage(loadError));
                }
            } finally {
                if (!cancelled) {
                    setLoading(false);
                }
            }
        }

        loadDoctors();
        return () => {
            cancelled = true;
        };
    }, []);

    useEffect(() => {
        if (!selectedDoctor?.id || !appointmentDate) {
            setAvailability(null);
            return undefined;
        }

        let cancelled = false;

        async function loadAvailability() {
            try {
                const result = await getDoctorAvailability(selectedDoctor.id, appointmentDate);
                if (!cancelled) {
                    setAvailability(result);
                }
            } catch {
                if (!cancelled) {
                    setAvailability(null);
                }
            }
        }

        loadAvailability();
        return () => {
            cancelled = true;
        };
    }, [selectedDoctor, appointmentDate]);

    const slots = useMemo(() => {
        if (availability?.advertisedSlots?.length) {
            return availability.advertisedSlots;
        }
        return selectedDoctor?.availabilitySlots || [];
    }, [availability, selectedDoctor]);

    async function confirmBooking() {
        setError("");

        if (!user?.id) {
            navigate("/login");
            return;
        }

        if (!selectedDoctor?.id || !appointmentDate || !slot || !reason.trim()) {
            setError("Please choose a date, time slot, and reason for visit.");
            return;
        }

        const times = parseSlotTimes(slot);
        if (!times) {
            setError("The selected time slot is not in a recognized format.");
            return;
        }

        setSubmitting(true);
        try {
            await createAppointment({
                doctorProfileId: selectedDoctor.id,
                appointmentDate,
                startTime: times.startTime,
                endTime: times.endTime,
                slotLabel: slot,
                reason: reason.trim(),
                notes: notes.trim() || undefined
            });
            navigate("/patient/my-appointment");
        } catch (bookingError) {
            setError(getApiErrorMessage(bookingError));
        } finally {
            setSubmitting(false);
        }
    }

    return (
        <div className="book-appointment-page">
            <h2 className="page-title">
                <span>Find a Doctor</span>
                <small>Book an appointment with the right healthcare professional.</small>
            </h2>

            {error && <p role="alert">{error}</p>}
            {loading && <p>Loading approved doctors...</p>}

            {!selectedDoctor && !loading && (
                <>
                    {doctors.length === 0 ? (
                        <div className="no-doctors">
                            <div className="no-doctors-icon" aria-hidden="true">
                                <svg width="42" height="42" viewBox="0 0 24 24" fill="none">
                                    <path d="M12 21a8 8 0 1 0 0-16 8 8 0 0 0 0 16Z" stroke="currentColor" strokeWidth="1.7" />
                                    <path d="M12 8v4l2.5 1.5M8.5 3.8 10 2m5.5 1.8L14 2" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" />
                                </svg>
                            </div>
                            <p className="no-doctors-title">
                                No doctors available right now
                            </p>
                            <p className="no-doctors-subtitle">
                                Approved doctors will appear here after an administrator reviews their profiles.
                            </p>
                        </div>
                    ) : (
                        <div className="doctor-grid">
                            {doctors.map((doc, index) => (
                                <ScrollReveal
                                    as="div"
                                    className="doctor-card"
                                    key={doc.id}
                                    delay={index * 80}
                                >
                                    <div className="doctor-placeholder">
                                        {doc.fullName?.charAt(0).toUpperCase()}
                                    </div>
                                    <h3>{doc.fullName}</h3>
                                    <p className="specialization">{doc.specialization}</p>
                                    <p className="info">Experience: {doc.experienceYears} years</p>
                                    <p className="info">Fee: ₹{doc.consultationFee}</p>
                                    {doc.availabilitySlots?.length > 0 && (
                                        <p className="info available-slots">
                                            Available: {doc.availabilitySlots.join(", ")}
                                        </p>
                                    )}
                                    <button onClick={() => setSelectedDoctor(doc)}>
                                        Book Appointment
                                    </button>
                                </ScrollReveal>
                            ))}
                        </div>
                    )}
                </>
            )}

            {selectedDoctor && (
                <div className="booking-wrapper">
                    <div className="booking-form">
                        <h3>{selectedDoctor.fullName}</h3>
                        <p className="info">{selectedDoctor.specialization}</p>
                        <input
                            type="date"
                            min={localIsoDate(0)}
                            value={appointmentDate}
                            onChange={(event) => {
                                setAppointmentDate(event.target.value);
                                setSlot("");
                            }}
                            required
                        />
                        <select
                            value={slot}
                            onChange={(event) => setSlot(event.target.value)}
                        >
                            <option value="">Select Time Slot</option>
                            {slots.map((time) => {
                                const booked = isSlotBooked(time, availability?.bookedRanges);
                                return (
                                    <option key={time} value={time} disabled={booked}>
                                        {time}{booked ? " (unavailable)" : ""}
                                    </option>
                                );
                            })}
                        </select>
                        <input
                            type="text"
                            placeholder="Reason for visit"
                            value={reason}
                            onChange={(event) => setReason(event.target.value)}
                            required
                        />
                        <textarea
                            placeholder="Notes (optional)"
                            value={notes}
                            onChange={(event) => setNotes(event.target.value)}
                        />
                        <button type="button" onClick={confirmBooking} disabled={submitting}>
                            {submitting ? "Booking..." : "Confirm Appointment"}
                        </button>
                        <button type="button" onClick={() => setSelectedDoctor(null)} disabled={submitting}>
                            Back to doctors
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
}

export default BookAppointment;
