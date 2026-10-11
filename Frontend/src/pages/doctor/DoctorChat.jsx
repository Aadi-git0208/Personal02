import React, { useEffect, useState } from "react";
import { getApiErrorMessage, pageContent } from "../../api/client";
import {
    closeConversation,
    listConversations,
    listMessages,
    markConversationRead,
    reopenConversation,
    sendMessage as sendChatMessage
} from "../../api/chatApi";
import { useAuth } from "../../auth/AuthContext";
import "./DoctorDashboard.css";

function DoctorChat() {
    const { user } = useAuth();
    const [conversations, setConversations] = useState([]);
    const [selectedId, setSelectedId] = useState("");
    const [conversation, setConversation] = useState(null);
    const [messages, setMessages] = useState([]);
    const [replyText, setReplyText] = useState("");
    const [loading, setLoading] = useState(true);
    const [sending, setSending] = useState(false);
    const [error, setError] = useState("");

    useEffect(() => {
        let cancelled = false;

        async function load() {
            setLoading(true);
            try {
                const page = await listConversations({ size: 50, sort: "lastMessageAt,desc" });
                if (!cancelled) {
                    setConversations(pageContent(page));
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

        load();
        return () => {
            cancelled = true;
        };
    }, []);

    useEffect(() => {
        if (!selectedId) {
            setConversation(null);
            setMessages([]);
            return undefined;
        }

        const selected = conversations.find((item) => String(item.id) === String(selectedId));
        setConversation(selected || null);

        let cancelled = false;

        async function loadMessages() {
            try {
                const page = await listMessages(selectedId, { size: 50, sort: "createdAt,asc" });
                if (!cancelled) {
                    setMessages(pageContent(page));
                }
                await markConversationRead(selectedId);
            } catch (loadError) {
                if (!cancelled) {
                    setError(getApiErrorMessage(loadError));
                }
            }
        }

        loadMessages();
        return () => {
            cancelled = true;
        };
    }, [selectedId, conversations]);

    async function sendReply() {
        if (!replyText.trim() || !selectedId) {
            return;
        }
        setSending(true);
        setError("");
        try {
            const created = await sendChatMessage(selectedId, replyText.trim());
            setMessages((current) => [...current, created]);
            setReplyText("");
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
            setConversations((current) =>
                current.map((item) => (item.id === updated.id ? updated : item))
            );
        } catch (toggleError) {
            setError(getApiErrorMessage(toggleError));
        }
    }

    return (
        <div className="doctor-dashboard no-sidebar">
            <main className="main-content">
                <h1>Messages</h1>
                <p className="subtitle">Conversations created from patient appointments</p>
                {error && <p role="alert">{error}</p>}
                {loading ? (
                    <p>Loading conversations...</p>
                ) : conversations.length === 0 ? (
                    <p>No conversations yet.</p>
                ) : (
                    <select
                        value={selectedId}
                        onChange={(event) => setSelectedId(event.target.value)}
                    >
                        <option value="">Select a conversation</option>
                        {conversations.map((item) => (
                            <option key={item.id} value={item.id}>
                                Patient #{item.patientUserId} · {item.status}
                            </option>
                        ))}
                    </select>
                )}

                {conversation && (
                    <div className="table-card" style={{ marginTop: "20px" }}>
                        <p>
                            {conversation.status === "CLOSED" ? "Closed" : "Open"}
                            <button type="button" onClick={toggleConversation} style={{ marginLeft: "12px" }}>
                                {conversation.status === "CLOSED" ? "Reopen" : "Close"}
                            </button>
                        </p>
                        <div>
                            {messages.length === 0 && <p>No messages yet</p>}
                            {messages.map((msg) => (
                                <p key={msg.id}>
                                    <strong>
                                        {String(msg.senderUserId) === String(user?.id) ? "You" : `Patient #${msg.senderUserId}`}
                                    </strong>
                                    : {msg.body}
                                </p>
                            ))}
                        </div>
                        <div>
                            <input
                                type="text"
                                placeholder="Type a reply"
                                value={replyText}
                                disabled={sending || conversation.status === "CLOSED"}
                                onChange={(event) => setReplyText(event.target.value)}
                            />
                            <button
                                type="button"
                                className="btn accept"
                                disabled={!replyText.trim() || sending || conversation.status === "CLOSED"}
                                onClick={sendReply}
                            >
                                {sending ? "Sending..." : "Send"}
                            </button>
                        </div>
                    </div>
                )}
            </main>
        </div>
    );
}

export default DoctorChat;
