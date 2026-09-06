import { useState, useEffect } from "react";
import "./ChatWithDoctor.css";

function ChatWithDoctor() {
    const patient =
        JSON.parse(localStorage.getItem("currentUser")) || {};

    const [doctors, setDoctors] = useState([]);
    const [selectedDoctorId, setSelectedDoctorId] = useState("");
    const [message, setMessage] = useState("");

    const [chats, setChats] = useState(() => {
        try {
            return (
                JSON.parse(
                    localStorage.getItem("chats")
                ) || []
            );
        } catch {
            return [];
        }
    });

    useEffect(() => {
        if (!patient?.id) return;

        const loadDoctors = () => {
            try {
                const appointments =
                    JSON.parse(
                        localStorage.getItem("appointments")
                    ) || [];

                const patientAppointments =
                    appointments.filter(
                        (app) =>
                            app.patientId === patient.id
                    );

                const doctorIds = new Set();
                const doctorMap = new Map();

                patientAppointments.forEach((app) => {
                    const doctorId =
                        app.doctorId ||
                        app.doctorUserId;

                    const doctorUserId =
                        app.doctorUserId ||
                        app.doctorId;

                    const uniqueKey = `${doctorId}-${doctorUserId}`;

                    if (
                        doctorId &&
                        !doctorIds.has(uniqueKey)
                    ) {
                        doctorIds.add(uniqueKey);

                        doctorMap.set(uniqueKey, {
                            id: doctorId,
                            userId: doctorUserId,
                            name:
                                app.doctorName ||
                                "Unknown Doctor",
                            appointmentDoctorId:
                                app.doctorId,
                            appointmentDoctorUserId:
                                app.doctorUserId
                        });
                    }
                });

                const doctorProfiles =
                    JSON.parse(
                        localStorage.getItem(
                            "doctorProfiles"
                        )
                    ) || [];

                doctorProfiles.forEach((profile) => {
                    const existing =
                        doctorMap.get(profile.id) ||
                        doctorMap.get(profile.userId);

                    if (existing) {
                        doctorMap.set(
                            profile.id ||
                                profile.userId,
                            {
                                ...existing,
                                name:
                                    profile.name ||
                                    existing.name,
                                id:
                                    profile.id ||
                                    profile.userId,
                                userId:
                                    profile.userId ||
                                    profile.id
                            }
                        );
                    }
                });

                setDoctors(
                    Array.from(doctorMap.values())
                );
            } catch (error) {
                console.error(
                    "Error loading doctors:",
                    error
                );
            }
        };

        loadDoctors();

        const interval = setInterval(
            loadDoctors,
            1000
        );

        window.addEventListener(
            "storage",
            loadDoctors
        );

        return () => {
            clearInterval(interval);

            window.removeEventListener(
                "storage",
                loadDoctors
            );
        };
    }, [patient?.id]);

    const selectedDoctor = doctors.find(
        (doctor) =>
            doctor.id === selectedDoctorId ||
            doctor.userId === selectedDoctorId
    );

    useEffect(() => {
        const loadChats = () => {
            try {
                const stored =
                    JSON.parse(
                        localStorage.getItem("chats")
                    ) || [];

                setChats(stored);
            } catch (error) {
                console.error(
                    "Error loading chats:",
                    error
                );
            }
        };

        loadChats();

        const interval = setInterval(
            loadChats,
            1000
        );

        window.addEventListener(
            "storage",
            loadChats
        );

        return () => {
            clearInterval(interval);

            window.removeEventListener(
                "storage",
                loadChats
            );
        };
    }, []);

    const messages =
        selectedDoctor && patient?.id
            ? chats
                  .filter((msg) => {
                      const matchesPatient =
                          msg.patientId ===
                          patient.id;

                      const matchesDoctor =
                          msg.doctorId ===
                              selectedDoctor.id ||
                          msg.doctorId ===
                              selectedDoctor.userId ||
                          msg.doctorUserId ===
                              selectedDoctor.id ||
                          msg.doctorUserId ===
                              selectedDoctor.userId;

                      return (
                          matchesPatient &&
                          matchesDoctor
                      );
                  })
                  .sort((a, b) => {
                      const timeA =
                          a.timestamp ||
                          a.time ||
                          "";

                      const timeB =
                          b.timestamp ||
                          b.time ||
                          "";

                      return timeA.localeCompare(
                          timeB
                      );
                  })
            : [];

    const sendMessage = () => {
        if (
            !message.trim() ||
            !selectedDoctor ||
            !patient?.id
        ) {
            return;
        }

        const doctorProfiles =
            JSON.parse(
                localStorage.getItem(
                    "doctorProfiles"
                )
            ) || [];

        const doctorProfile =
            doctorProfiles.find(
                (profile) =>
                    profile.id ===
                        selectedDoctor.id ||
                    profile.id ===
                        selectedDoctor.userId ||
                    profile.userId ===
                        selectedDoctor.id ||
                    profile.userId ===
                        selectedDoctor.userId ||
                    profile.id ===
                        selectedDoctor.appointmentDoctorId ||
                    profile.userId ===
                        selectedDoctor.appointmentDoctorUserId
            );

        const doctorId =
            selectedDoctor.appointmentDoctorId ||
            selectedDoctor.id ||
            doctorProfile?.id ||
            selectedDoctor.userId;

        const doctorUserId =
            selectedDoctor.appointmentDoctorUserId ||
            selectedDoctor.userId ||
            doctorProfile?.userId ||
            doctorProfile?.id ||
            selectedDoctor.id;

        const newMessage = {
            id: crypto.randomUUID(),
            from: "patient",
            to: "doctor",
            patientId: patient.id,
            patientName:
                patient.name || "Patient",
            doctorId: doctorId,
            doctorUserId: doctorUserId,
            doctorName:
                selectedDoctor.name,
            message: message.trim(),
            time: new Date().toLocaleTimeString(),
            timestamp:
                new Date().toISOString()
        };

        const updatedChats = [
            ...chats,
            newMessage
        ];

        localStorage.setItem(
            "chats",
            JSON.stringify(updatedChats)
        );

        setChats(updatedChats);
        setMessage("");

        window.dispatchEvent(
            new Event("storage")
        );
    };

    return (
        <div className="chat-container">
            <h2 className="chat-title">
                Chat with Doctor
            </h2>

            {doctors.length === 0 ? (
                <div className="no-doctors-chat">
                    <p>
                        No doctors available.
                        Please book an appointment
                        first.
                    </p>
                </div>
            ) : (
                <select
                    className="doctor-select"
                    value={selectedDoctorId}
                    onChange={(event) =>
                        setSelectedDoctorId(
                            event.target.value
                        )
                    }
                >
                    <option value="">
                        Select Doctor
                    </option>

                    {doctors.map((doctor) => (
                        <option
                            key={
                                doctor.id ||
                                doctor.userId
                            }
                            value={
                                doctor.id ||
                                doctor.userId
                            }
                        >
                            {doctor.name}
                        </option>
                    ))}
                </select>
            )}

            {selectedDoctor && (
                <div className="chat-box">
                    <div className="messages">
                        {messages.length === 0 && (
                            <p className="empty">
                                No messages yet
                            </p>
                        )}

                        {messages.map((msg) => (
                            <div
                                key={msg.id}
                                className={`message ${
                                    msg.from ===
                                    "patient"
                                        ? "patient"
                                        : "doctor"
                                }`}
                            >
                                <span>
                                    {msg.message}
                                </span>

                                <small>
                                    {msg.time}
                                </small>
                            </div>
                        ))}

                        {messages.length > 0 && (
                            <div
                                ref={(element) => {
                                    if (element) {
                                        setTimeout(
                                            () =>
                                                element.scrollIntoView(
                                                    {
                                                        behavior:
                                                            "smooth"
                                                    }
                                                ),
                                            100
                                        );
                                    }
                                }}
                            />
                        )}
                    </div>

                    <div className="chat-input">
                        <input
                            type="text"
                            placeholder="Type your message..."
                            value={message}
                            onChange={(event) =>
                                setMessage(
                                    event.target.value
                                )
                            }
                            onKeyDown={(event) => {
                                if (
                                    event.key ===
                                        "Enter" &&
                                    !event.shiftKey
                                ) {
                                    event.preventDefault();
                                    sendMessage();
                                }
                            }}
                        />

                        <button
                            onClick={sendMessage}
                            disabled={!message.trim()}
                        >
                            Send
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
}

export default ChatWithDoctor;