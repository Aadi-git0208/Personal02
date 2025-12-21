import React, { useState } from "react";

function DoctorChat() {
  const doctor = JSON.parse(localStorage.getItem("currentUser"));

  // Read all chats (single source of truth)
  const chats = JSON.parse(localStorage.getItem("chats")) || [];

  // Only messages sent TO this doctor
  const messages = chats.filter(
    (msg) => msg.to === "doctor" && msg.doctorId === doctor?.id
  );

  // Store replies per patient
  const [replies, setReplies] = useState({});

  const sendReply = (msg) => {
    const replyText = replies[msg.id];
    if (!replyText?.trim()) return;

    const newReply = {
      id: crypto.randomUUID(),
      from: "doctor",
      to: "patient",
      patientId: msg.patientId,
      patientName: msg.patientName,
      doctorId: doctor.id,
      doctorName: doctor.name,
      message: replyText,
      time: new Date().toLocaleTimeString(),
    };

    localStorage.setItem(
      "chats",
      JSON.stringify([...chats, newReply])
    );

    // Clear reply input for this message
    setReplies((prev) => ({
      ...prev,
      [msg.id]: "",
    }));
  };

  return (
    <>
      {/* ===== INLINE CSS ===== */}
      <style>{`
        .doctor-chat-page {
          margin-top: 80px;
          padding: 20px;
          background: #f4f6f9;
          min-height: 100vh;
        }

        .doctor-chat-page h2 {
          margin-bottom: 20px;
          color: #0d2656;
        }

        .chat-card {
          background: #0d2656;
          color: white;
          padding: 18px;
          border-radius: 14px;
          margin-bottom: 18px;
          box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
        }

        .chat-card p {
          margin: 6px 0;
        }

        .chat-card small {
          color: #cbd5f5;
          font-size: 12px;
        }

        .chat-message {
          margin: 12px 0;
          font-size: 15px;
          line-height: 1.4;
        }

        .chat-card textarea {
          width: 100%;
          min-height: 70px;
          margin-top: 12px;
          padding: 10px;
          border-radius: 8px;
          border: none;
          outline: none;
          resize: none;
          font-family: inherit;
        }

        .chat-card button {
          margin-top: 10px;
          padding: 8px 16px;
          background: #22c55e;
          color: #052e16;
          border: none;
          border-radius: 8px;
          font-weight: 600;
          cursor: pointer;
        }
      `}</style>

      {/* ===== UI ===== */}
      <div className="doctor-chat-page">
        <h2>Patient Messages</h2>

        {messages.length === 0 && <p>No messages yet</p>}

        {messages.map((msg) => (
          <div key={msg.id} className="chat-card">
            <p>
              <strong>From:</strong> {msg.patientName}
            </p>

            <p className="chat-message">{msg.message}</p>
            <small>{msg.time}</small>

            <textarea
              placeholder="Type reply..."
              value={replies[msg.id] || ""}
              onChange={(e) =>
                setReplies({
                  ...replies,
                  [msg.id]: e.target.value,
                })
              }
            />

            <button onClick={() => sendReply(msg)}>
              Send Reply
            </button>
          </div>
        ))}
      </div>
    </>
  );
}

export default DoctorChat;
