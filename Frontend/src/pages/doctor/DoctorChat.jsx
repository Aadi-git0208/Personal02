import React, { useState, useEffect } from "react";
import "./DoctorDashboard.css";

function DoctorChat() {
  const doctor = JSON.parse(localStorage.getItem("currentUser")) || {};

  const [patients, setPatients] = useState([]);
  const [selectedPatientId, setSelectedPatientId] = useState("");
  const [chats, setChats] = useState([]);
  const [replyText, setReplyText] = useState("");
  const [allMessages, setAllMessages] = useState([]);

  // Redirect if not logged in as doctor
  useEffect(() => {
    if (!doctor?.name || doctor?.role !== "doctor") {
      // Don't redirect if no doctor, just show empty state
      return;
    }
  }, [doctor]);

  // Load patients who have booked appointments with this doctor
  useEffect(() => {
    if (!doctor?.id && !doctor?.userId) return;

    const loadPatients = () => {
      try {
        const appointments = JSON.parse(localStorage.getItem("appointments")) || [];
        const allChats = JSON.parse(localStorage.getItem("chats")) || [];
        
        // Filter appointments for this doctor
        const doctorAppointments = appointments.filter(
          (app) =>
            app.doctorId === doctor.id ||
            app.doctorId === doctor.userId ||
            app.doctorUserId === doctor.id ||
            app.doctorUserId === doctor.userId
        );

        // Get unique patients from appointments
        const patientMap = new Map();
        doctorAppointments.forEach((app) => {
          if (app.patientId && app.patientName) {
            if (!patientMap.has(app.patientId)) {
              patientMap.set(app.patientId, {
                id: app.patientId,
                name: app.patientName,
                appointmentId: app.id,
                unreadCount: 0,
              });
            }
          }
        });

        // Also check messages to find patients who have messaged
        allChats.forEach((msg) => {
          const matchesDoctor = 
            msg.doctorId === doctor.id ||
            msg.doctorId === doctor.userId ||
            msg.doctorUserId === doctor.id ||
            msg.doctorUserId === doctor.userId;
          
          if (matchesDoctor && msg.patientId && msg.patientName) {
            if (!patientMap.has(msg.patientId)) {
              patientMap.set(msg.patientId, {
                id: msg.patientId,
                name: msg.patientName,
                appointmentId: null,
                unreadCount: 0,
              });
            }
          }
        });

        // Count unread messages (messages from patients that haven't been replied to recently)
        const patientArray = Array.from(patientMap.values());
        patientArray.forEach((patient) => {
          const patientMessages = allChats.filter(
            (msg) =>
              msg.patientId === patient.id &&
              (msg.doctorId === doctor.id ||
               msg.doctorId === doctor.userId ||
               msg.doctorUserId === doctor.id ||
               msg.doctorUserId === doctor.userId) &&
              msg.from === "patient"
          );
          patient.unreadCount = patientMessages.length;
        });

        setPatients(patientArray);
      } catch (err) {
        console.error("Error loading patients:", err);
      }
    };

    loadPatients();

    // Refresh periodically
    const interval = setInterval(loadPatients, 2000);
    return () => clearInterval(interval);
  }, [doctor?.id, doctor?.userId]);

  // Load and filter messages for selected patient
  useEffect(() => {
    if (!selectedPatientId || (!doctor?.id && !doctor?.userId)) {
      setChats([]);
      return;
    }

    const loadMessages = () => {
      try {
        const allChats = JSON.parse(localStorage.getItem("chats")) || [];

        // Filter messages for this doctor-patient pair
        // Check all possible ID combinations to ensure we catch all messages
        const filteredChats = allChats.filter((msg) => {
          const matchesPatient = msg.patientId === selectedPatientId;
          
          // Check doctor ID matching - be very flexible with all ID variations
          const doctorIdMatch = 
            msg.doctorId === doctor.id ||
            msg.doctorId === doctor.userId ||
            msg.doctorUserId === doctor.id ||
            msg.doctorUserId === doctor.userId ||
            // Also check if the message is directed to this doctor
            (msg.to === "doctor" && (
              msg.doctorId === doctor.id ||
              msg.doctorId === doctor.userId ||
              msg.doctorUserId === doctor.id ||
              msg.doctorUserId === doctor.userId
            ));
          
          return matchesPatient && doctorIdMatch;
        });
        
        // Also store all messages for this doctor (for debugging/display)
        const allDoctorMessages = allChats.filter((msg) => {
          return msg.doctorId === doctor.id ||
                 msg.doctorId === doctor.userId ||
                 msg.doctorUserId === doctor.id ||
                 msg.doctorUserId === doctor.userId;
        });
        setAllMessages(allDoctorMessages);

        // Sort by timestamp
        filteredChats.sort((a, b) => {
          const timeA = a.timestamp || a.time || "";
          const timeB = b.timestamp || b.time || "";
          return timeA.localeCompare(timeB);
        });

        setChats(filteredChats);
      } catch (err) {
        console.error("Error loading messages:", err);
      }
    };

    loadMessages();

    // Refresh messages periodically
    const interval = setInterval(loadMessages, 1000);
    return () => clearInterval(interval);
  }, [selectedPatientId, doctor?.id, doctor?.userId]);

  // Get selected patient object
  const selectedPatient = patients.find((p) => p.id === selectedPatientId);

  // Handle patient selection
  const handlePatientSelect = (patientId) => {
    setSelectedPatientId(patientId);
    setReplyText(""); // Clear reply when switching patients
  };

  // Send reply
  const sendReply = () => {
    if (!replyText.trim() || !selectedPatient || (!doctor?.id && !doctor?.userId)) return;

    const allChats = JSON.parse(localStorage.getItem("chats")) || [];

    const newReply = {
      id: crypto.randomUUID(),
      from: "doctor",
      to: "patient",
      patientId: selectedPatient.id,
      patientName: selectedPatient.name,
      doctorId: doctor.id || doctor.userId,
      doctorUserId: doctor.userId || doctor.id,
      doctorName: doctor.name || "Doctor",
      message: replyText.trim(),
      time: new Date().toLocaleTimeString(),
      timestamp: new Date().toISOString(),
    };

    localStorage.setItem("chats", JSON.stringify([...allChats, newReply]));
    setReplyText("");
    
    // Trigger immediate update by reloading messages
    const updatedChats = [...allChats, newReply];
    const filteredChats = updatedChats.filter(
      (msg) =>
        msg.patientId === selectedPatient.id &&
        (msg.doctorId === doctor.id ||
         msg.doctorId === doctor.userId ||
         msg.doctorUserId === doctor.id ||
         msg.doctorUserId === doctor.userId)
    );
    filteredChats.sort((a, b) => {
      const timeA = a.timestamp || a.time || "";
      const timeB = b.timestamp || b.time || "";
      return timeA.localeCompare(timeB);
    });
    setChats(filteredChats);
  };

  // Early return if no doctor
  if (!doctor?.id && !doctor?.userId) {
    return (
      <div className="doctor-dashboard no-sidebar">
        <main className="main-content">
          <h1>Patient Messages</h1>
          <div style={{ textAlign: "center", padding: "40px" }}>
            <p style={{ opacity: 0.7 }}>Please log in as a doctor to view messages.</p>
          </div>
        </main>
      </div>
    );
  }

  return (
    <div className="doctor-dashboard no-sidebar">
      <main className="main-content">
        <h1>Patient Messages</h1>
        <p className="subtitle">Chat with your patients</p>

        {patients.length === 0 ? (
          <div style={{ textAlign: "center", padding: "40px" }}>
            <p style={{ opacity: 0.7 }}>
              No patients have booked appointments yet.
            </p>
            <p style={{ fontSize: "12px", opacity: 0.5, marginTop: "10px" }}>
              Once patients book appointments with you, they will appear here.
            </p>
          </div>
        ) : (
          <div className="doctor-chat-container">
            {/* Patient List */}
            <div className="patient-list">
              <h3>Patients ({patients.length})</h3>
              {patients.length === 0 ? (
                <p style={{ opacity: 0.7, padding: "10px" }}>No patients yet</p>
              ) : (
                patients.map((patient) => (
                  <div
                    key={patient.id}
                    className={`patient-item ${
                      selectedPatientId === patient.id ? "active" : ""
                    }`}
                    onClick={() => handlePatientSelect(patient.id)}
                  >
                    <div className="patient-avatar">
                      {patient.name.charAt(0).toUpperCase()}
                    </div>
                    <div className="patient-info">
                      <div className="patient-name">{patient.name}</div>
                      {patient.unreadCount > 0 && (
                        <div className="unread-badge">{patient.unreadCount}</div>
                      )}
                    </div>
                  </div>
                ))
              )}
            </div>

            {/* Chat Area */}
            <div className="chat-area">
              {selectedPatient ? (
                <>
                  <div className="chat-header">
                    <h3>Conversation with {selectedPatient.name}</h3>
                  </div>

                  <div className="chat-messages">
                    {chats.length === 0 ? (
                      <div className="empty-chat">
                        <p>No messages yet. Start the conversation!</p>
                        <p style={{ fontSize: "12px", opacity: 0.5, marginTop: "10px" }}>
                          Patient messages will appear here once they send a message.
                        </p>
                      </div>
                    ) : (
                      <>
                        {chats.map((msg) => (
                          <div
                            key={msg.id}
                            className={`chat-message ${
                              msg.from === "doctor" ? "doctor-msg" : "patient-msg"
                            }`}
                          >
                            <div className="message-content">
                              <div className="message-text">{msg.message}</div>
                              <div className="message-time">{msg.time}</div>
                            </div>
                          </div>
                        ))}
                        {/* Auto-scroll to bottom */}
                        <div ref={(el) => {
                          if (el) {
                            setTimeout(() => el.scrollIntoView({ behavior: 'smooth' }), 100);
                          }
                        }} />
                      </>
                    )}
                  </div>

                  <div className="chat-input-area">
                    <textarea
                      placeholder="Type your reply..."
                      value={replyText}
                      onChange={(e) => setReplyText(e.target.value)}
                      onKeyDown={(e) => {
                        if (e.key === "Enter" && !e.shiftKey) {
                          e.preventDefault();
                          sendReply();
                        }
                      }}
                      rows="3"
                    />
                    <button onClick={sendReply} disabled={!replyText.trim()}>
                      Send Reply
                    </button>
                  </div>
                </>
              ) : (
                <div className="no-selection">
                  <p>Select a patient to view messages</p>
                  {allMessages.length > 0 && (
                    <div style={{ marginTop: "20px", padding: "15px", background: "rgba(255,255,255,0.05)", borderRadius: "8px" }}>
                      <p style={{ fontSize: "12px", opacity: 0.7 }}>
                        Total messages for this doctor: {allMessages.length}
                      </p>
                      <p style={{ fontSize: "11px", opacity: 0.5, marginTop: "5px" }}>
                        Doctor ID: {doctor.id || doctor.userId || "N/A"}
                      </p>
                    </div>
                  )}
                </div>
              )}
            </div>
          </div>
        )}
      </main>
    </div>
  );
}

export default DoctorChat;
