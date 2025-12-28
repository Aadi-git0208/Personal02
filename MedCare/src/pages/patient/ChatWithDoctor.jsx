import { useState, useEffect } from "react";
import "./ChatWithDoctor.css";

function ChatWithDoctor() {
  const patient = JSON.parse(localStorage.getItem("currentUser")) || {};

  const [doctors, setDoctors] = useState([]);
  const [selectedDoctorId, setSelectedDoctorId] = useState("");
  const [message, setMessage] = useState("");

  // Load doctors from appointments (only doctors the patient has booked with)
  useEffect(() => {
    if (!patient?.id) return;

    const appointments = JSON.parse(localStorage.getItem("appointments")) || [];
    const patientAppointments = appointments.filter(
      (app) => app.patientId === patient.id
    );

    // Get unique doctors from appointments
    const doctorIds = new Set();
    const doctorMap = new Map();

    patientAppointments.forEach((app) => {
      // Use both doctorId and doctorUserId from appointment
      const doctorId = app.doctorId || app.doctorUserId;
      const doctorUserId = app.doctorUserId || app.doctorId;
      
      // Create a unique key that includes both IDs
      const uniqueKey = `${doctorId}-${doctorUserId}`;
      
      if (doctorId && !doctorIds.has(uniqueKey)) {
        doctorIds.add(uniqueKey);
        doctorMap.set(uniqueKey, {
          id: doctorId,
          userId: doctorUserId,
          name: app.doctorName || "Unknown Doctor",
          // Store both IDs for matching
          appointmentDoctorId: app.doctorId,
          appointmentDoctorUserId: app.doctorUserId,
        });
      }
    });

    // Also check doctor profiles for additional info
    const doctorProfiles = JSON.parse(localStorage.getItem("doctorProfiles")) || [];
    doctorProfiles.forEach((profile) => {
      if (doctorMap.has(profile.id) || doctorMap.has(profile.userId)) {
        const existing = doctorMap.get(profile.id) || doctorMap.get(profile.userId);
        if (existing) {
          doctorMap.set(profile.id || profile.userId, {
            ...existing,
            name: profile.name,
            id: profile.id || profile.userId,
            userId: profile.userId || profile.id,
          });
        }
      }
    });

    setDoctors(Array.from(doctorMap.values()));
  }, [patient?.id]);

  /* 🔹 Get selected doctor object */
  const selectedDoctor = doctors.find(
    (doc) => doc.id === selectedDoctorId || doc.userId === selectedDoctorId
  );

  /* 🔹 Read ALL chats from localStorage (single source of truth) */
  const [chats, setChats] = useState(() => {
    try {
      return JSON.parse(localStorage.getItem("chats")) || [];
    } catch {
      return [];
    }
  });

  // Update chats when localStorage changes
  useEffect(() => {
    const loadChats = () => {
      try {
        const stored = JSON.parse(localStorage.getItem("chats")) || [];
        setChats(stored);
      } catch (err) {
        console.error("Error loading chats:", err);
      }
    };

    loadChats();
    const interval = setInterval(loadChats, 1000);
    window.addEventListener('storage', loadChats);
    
    return () => {
      clearInterval(interval);
      window.removeEventListener('storage', loadChats);
    };
  }, []);

  /* 🔹 Derive messages */
  const messages = selectedDoctor && patient?.id
    ? chats
        .filter((msg) => {
          const matchesPatient = msg.patientId === patient.id;
          const matchesDoctor = 
            msg.doctorId === selectedDoctor.id || 
            msg.doctorId === selectedDoctor.userId ||
            msg.doctorUserId === selectedDoctor.id ||
            msg.doctorUserId === selectedDoctor.userId;
          return matchesPatient && matchesDoctor;
        })
        .sort((a, b) => {
          const timeA = a.timestamp || a.time || "";
          const timeB = b.timestamp || b.time || "";
          return timeA.localeCompare(timeB);
        })
    : [];

  /* 🔹 Send message */
  const sendMessage = () => {
    if (!message.trim() || !selectedDoctor || !patient?.id) return;

    // Get doctor profile to ensure we have the correct IDs
    const doctorProfiles = JSON.parse(localStorage.getItem("doctorProfiles")) || [];
    const doctorProfile = doctorProfiles.find(
      (p) => p.id === selectedDoctor.id || 
             p.id === selectedDoctor.userId ||
             p.userId === selectedDoctor.id ||
             p.userId === selectedDoctor.userId ||
             p.id === selectedDoctor.appointmentDoctorId ||
             p.userId === selectedDoctor.appointmentDoctorUserId
    );

    // Use appointment IDs first (most reliable), then profile IDs, then fallback
    const doctorId = selectedDoctor.appointmentDoctorId || 
                     selectedDoctor.id || 
                     doctorProfile?.id || 
                     selectedDoctor.userId;
    const doctorUserId = selectedDoctor.appointmentDoctorUserId || 
                         selectedDoctor.userId || 
                         doctorProfile?.userId || 
                         doctorProfile?.id || 
                         selectedDoctor.id;

    const newMessage = {
      id: crypto.randomUUID(), 
      from: "patient",
      to: "doctor",
      patientId: patient.id,
      patientName: patient.name || "Patient",
      doctorId: doctorId,
      doctorUserId: doctorUserId,
      doctorName: selectedDoctor.name,
      message: message.trim(),
      time: new Date().toLocaleTimeString(),
      timestamp: new Date().toISOString(),
    };

    const updatedChats = [...chats, newMessage];
    localStorage.setItem("chats", JSON.stringify(updatedChats));
    setChats(updatedChats); // Update state immediately

    setMessage("");
    
    // Force re-render by dispatching storage event
    window.dispatchEvent(new Event('storage'));
  };

  return (
    <div className="chat-container">
      <h2>Chat with Doctor</h2>

      {/* ===== Doctor Selection ===== */}
      {doctors.length === 0 ? (
        <div style={{ textAlign: "center", padding: "20px" }}>
          <p style={{ opacity: 0.7 }}>
            No doctors available. Please book an appointment first.
          </p>
        </div>
      ) : (
        <select
          className="doctor-select"
          value={selectedDoctorId}
          onChange={(e) => setSelectedDoctorId(e.target.value)}
        >
          <option value="">Select Doctor</option>
          {doctors.map((doc) => (
            <option key={doc.id || doc.userId} value={doc.id || doc.userId}>
              {doc.name}
            </option>
          ))}
        </select>
      )}

      {/* ===== Chat Box ===== */}
      {selectedDoctor && (
        <div className="chat-box">
          <div className="messages">
            {messages.length === 0 && (
              <p className="empty">No messages yet</p>
            )}

            {messages.map((msg) => (
              <div
                key={msg.id}
                className={`message ${
                  msg.from === "patient" ? "patient" : "doctor"
                }`}
              >
                <span>{msg.message}</span>
                <small>{msg.time}</small>
              </div>
            ))}
            {/* Auto-scroll to bottom */}
            {messages.length > 0 && (
              <div ref={(el) => {
                if (el) {
                  setTimeout(() => el.scrollIntoView({ behavior: 'smooth' }), 100);
                }
              }} />
            )}
          </div>

          <div className="chat-input">
            <input
              type="text"
              placeholder="Type your message..."
              value={message}
              onChange={(e) => setMessage(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === "Enter" && !e.shiftKey) {
                  e.preventDefault();
                  sendMessage();
                }
              }}
            />
            <button onClick={sendMessage} disabled={!message.trim()}>
              Send
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

export default ChatWithDoctor;
