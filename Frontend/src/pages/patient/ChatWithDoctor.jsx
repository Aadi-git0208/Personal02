import { useEffect, useMemo, useState } from "react";
import { getApiErrorMessage, pageContent } from "../../api/client";
import { listPatientAppointments } from "../../api/appointmentApi";
import {
    closeConversation,
    createConversation,
    listMessages,
    markConversationRead,
    reopenConversation,
    sendMessage as sendChatMessage
} from "../../api/chatApi";
import { useAuth } from "../../auth/AuthContext";
import "./ChatWithDoctor.css";

function ChatWithDoctor() {
    const { user } = useAuth();
    const [doctors, setDoctors] = useState([]);
    const [selectedAppointmentId, setSelectedAppointmentId] = useState("");
    const [conversation, setConversation] = useState(null);
    const [messages, setMessages] = useState([]);
    const [message, setMessage] = useState("");
    const [loading, setLoading] = useState(true);
    const [sending, setSending] = useState(false);
    const [error, setError] = useState("");

    useEffect(() => {
        let cancelled = false;

        async function loadDoctors() {
            setLoading(true);
            setError("");
            try {
                const page = await listPatientAppointments({ size: 50, sort: "appointmentDate,desc" });
                const appointments = pageContent(page);
                const unique = [];
                const seen = new Set();
                appointments.forEach((appointment) => {
                    if (appointment.status === "CANCELLED" || appointment.status === "REJECTED") {
                        return;
                    }
                    const key = String(appointment.doctorProfileId);
                    if (!seen.has(key)) {
                        seen.add(key);
                        unique.push({
                            appointmentId: appointment.id,
                            doctorProfileId: appointment.doctorProfileId,
                            doctorUserId: appointment.doctorUserId,
                            name: appointment.doctorFullName || `Doctor #${appointment.doctorUserId}`
                        });
                    }
                });
                if (!cancelled) {
                    setDoctors(unique);
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

    const selectedDoctor = useMemo(
        () => doctors.find((doctor) => String(doctor.appointmentId) === String(selectedAppointmentId)),
        [doctors, selectedAppointmentId]
    );

    useEffect(() => {
        if (!selectedDoctor) {
            setConversation(null);
            setMessages([]);
            return undefined;
        }

        let cancelled = false;

        async function openConversation() {
            setError("");
            try {
                const created = await createConversation({ appointmentId: selectedDoctor.appointmentId });
                if (cancelled) {
                    return;
                }
                setConversation(created);
                const page = await listMessages(created.id, { size: 50, sort: "createdAt,asc" });
                if (!cancelled) {
                    setMessages(pageContent(page));
                }
                await markConversationRead(created.id);
            } catch (openError) {
                if (!cancelled) {
                    setError(getApiErrorMessage(openError));
                    setConversation(null);
                    setMessages([]);
                }
            }
        }

        openConversation();
        return () => {
            cancelled = true;
        };
    }, [selectedDoctor]);

    async function sendMessage() {
        if (!message.trim() || !conversation?.id) {
            return;
        }

        setSending(true);
        setError("");
        try {
            const created = await sendChatMessage(conversation.id, message.trim());
            setMessages((current) => [...current, created]);
            setMessage("");
        } catch (sendError) {
            setError(getApiErrorMessage(sendError));
        } finally {
            setSending(false);
        }
    }

    async function toggleConversation() {
        if (!conversation?.id) {
            return;
        }
        try {
            const updated = conversation.status === "CLOSED"
                ? await reopenConversation(conversation.id)
                : await closeConversation(conversation.id);
            setConversation(updated);
        } catch (toggleError) {
            setError(getApiErrorMessage(toggleError));
        }
    }

    return (
        <div className="chat-container">
            <h2 className="chat-title">Chat with Doctor</h2>
            {error && <p role="alert">{error}</p>}
            {loading && <p>Loading conversations...</p>}

            {!loading && doctors.length === 0 ? (
                <div className="no-doctors-chat">
                    <p>No doctors available. Please book an appointment first.</p>
                </div>
            ) : (
                <select
                    className="doctor-select"
                    value={selectedAppointmentId}
                    onChange={(event) => setSelectedAppointmentId(event.target.value)}
                >
                    <option value="">Select Doctor</option>
                    {doctors.map((doctor) => (
                        <option key={doctor.appointmentId} value={doctor.appointmentId}>
                            {doctor.name}
                        </option>
                    ))}
                </select>
            )}

            {selectedDoctor && conversation && (
                <div className="chat-box">
                    <p>
                        {conversation.status === "CLOSED" ? "This conversation is closed." : "Conversation is open."}
                        <button type="button" onClick={toggleConversation}>
                            {conversation.status === "CLOSED" ? "Reopen" : "Close"}
                        </button>
                    </p>
                    <div className="messages">
                        {messages.length === 0 && (
                            <p className="empty">No messages yet</p>
                        )}
                        {messages.map((msg) => (
                            <div
                                key={msg.id}
                                className={`message ${String(msg.senderUserId) === String(user?.id) ? "patient" : "doctor"}`}
                            >
                                <span>{msg.body}</span>
                                <small>{msg.createdAt ? new Date(msg.createdAt).toLocaleTimeString() : ""}</small>
                            </div>
                        ))}
                    </div>
                    <div className="chat-input">
                        <input
                            type="text"
                            placeholder="Type your message..."
                            value={message}
                            disabled={sending || conversation.status === "CLOSED"}
                            onChange={(event) => setMessage(event.target.value)}
                            onKeyDown={(event) => {
                                if (event.key === "Enter" && !event.shiftKey) {
                                    event.preventDefault();
                                    sendMessage();
                                }
                            }}
                        />
                        <button
                            onClick={sendMessage}
                            disabled={!message.trim() || sending || conversation.status === "CLOSED"}
                        >
                            {sending ? "Sending..." : "Send"}
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
}

export default ChatWithDoctor;
