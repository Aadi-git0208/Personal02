import { useState } from "react";
import "./ChatWithDoctor.css";

/* Doctor list with IDs (IMPORTANT) */
const doctors = [
  { id: "d1", name: "Dr. Peter Doe" },
  { id: "d2", name: "Dr. Sarah William" },
  { id: "d3", name: "Dr. John Smith" },
  { id: "d4", name: "Dr. Nandani Sharma" },
];

function ChatWithDoctor() {
  const patient = JSON.parse(localStorage.getItem("currentUser"));

  const [selectedDoctorId, setSelectedDoctorId] = useState("");
  const [message, setMessage] = useState("");

  /* 🔹 Get selected doctor object */
  const selectedDoctor = doctors.find(
    (doc) => doc.id === selectedDoctorId
  );

  /* 🔹 Read ALL chats from localStorage (single source of truth) */
  const chats = JSON.parse(localStorage.getItem("chats")) || [];

  /* 🔹 Derive messages (NO useEffect, NO setState) */
  const messages = selectedDoctor
    ? chats.filter(
        (msg) =>
          msg.patientId === patient.id &&
          msg.doctorId === selectedDoctor.id
      )
    : [];

  /* 🔹 Send message */
  const sendMessage = () => {
    if (!message.trim() || !selectedDoctor) return;

    const newMessage = {
      id: crypto.randomUUID(), 
      from: "patient",
      to: "doctor",
      patientId: patient.id,
      patientName: patient.name,
      doctorId: selectedDoctor.id,
      doctorName: selectedDoctor.name,
      message: message,
      time: new Date().toLocaleTimeString(),
    };

    localStorage.setItem(
      "chats",
      JSON.stringify([...chats, newMessage])
    );

    setMessage("");
  };

  return (
    <div className="chat-container">
      <h2>Chat with Doctor</h2>

      {/* ===== Doctor Selection ===== */}
      <select
        className="doctor-select"
        value={selectedDoctorId}
        onChange={(e) => setSelectedDoctorId(e.target.value)}
      >
        <option value="">Select Doctor</option>
        {doctors.map((doc) => (
          <option key={doc.id} value={doc.id}>
            {doc.name}
          </option>
        ))}
      </select>

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
          </div>

          <div className="chat-input">
            <input
              type="text"
              placeholder="Type your message..."
              value={message}
              onChange={(e) => setMessage(e.target.value)}
            />
            <button onClick={sendMessage}>Send</button>
          </div>
        </div>
      )}
    </div>
  );
}

export default ChatWithDoctor;
