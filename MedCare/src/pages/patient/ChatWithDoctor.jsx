import { useState } from "react";
import "./ChatWithDoctor.css";

const doctors = [
  "Dr. Peter Doe",
  "Dr. Sarah William",
  "Dr. John Smith",
  "Dr. Nandani Sharma",
    "Dr. Rahul Verma",
    "Dr. Anjali Mehta",
    "Dr. Amit Kapoor",
    "Dr. Vikram Singh",
    "Dr. Meera Iyer",
    "Dr. Karan Malhotra",
];

function ChatWithDoctor() {
  const [selectedDoctor, setSelectedDoctor] = useState("");
  const [message, setMessage] = useState("");
  const [messages, setMessages] = useState([]);

  const handleDoctorChange = (e) => {
    const doctor = e.target.value;
    setSelectedDoctor(doctor);

    if (!doctor) {
      setMessages([]);
      return;
    }

    const savedChat =
      JSON.parse(localStorage.getItem(`chat_${doctor}`)) || [];

    setMessages(savedChat);
  };

  const sendMessage = () => {
    if (!message.trim() || !selectedDoctor) return;

    const newMessage = {
      text: message,
      sender: "patient",
      time: new Date().toLocaleTimeString(),
    };

    const updatedMessages = [...messages, newMessage];
    setMessages(updatedMessages);

    localStorage.setItem(
      `chat_${selectedDoctor}`,
      JSON.stringify(updatedMessages)
    );

    setMessage("");
  };

  return (
    <div className="chat-container">
      <h2>Chat with Doctor</h2>

      {/* Doctor Selection */}
      <select
        className="doctor-select"
        value={selectedDoctor}
        onChange={handleDoctorChange}
      >
        <option value="">Select Doctor</option>
        {doctors.map((doc, index) => (
          <option key={index} value={doc}>
            {doc}
          </option>
        ))}
      </select>

      {/* Chat Box */}
      {selectedDoctor && (
        <div className="chat-box">
          <div className="messages">
            {messages.length === 0 && (
              <p className="empty">No messages yet</p>
            )}

            {messages.map((msg, index) => (
              <div key={index} className={`message ${msg.sender}`}>
                <span>{msg.text}</span>
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
