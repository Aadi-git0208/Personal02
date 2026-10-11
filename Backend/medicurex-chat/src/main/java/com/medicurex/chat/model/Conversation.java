package com.medicurex.chat.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(
        name = "conversations",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_chat_conversation_key", columnNames = "conversation_key"),
                @UniqueConstraint(name = "uk_chat_appointment", columnNames = "appointment_id")
        },
        indexes = {
                @Index(name = "idx_chat_patient", columnList = "patient_user_id"),
                @Index(name = "idx_chat_doctor", columnList = "doctor_user_id"),
                @Index(name = "idx_chat_status", columnList = "status"),
                @Index(name = "idx_chat_last_message", columnList = "last_message_at")
        }
)
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_user_id", nullable = false)
    private Long patientUserId;

    @Column(name = "doctor_user_id", nullable = false)
    private Long doctorUserId;

    @Column(name = "appointment_id")
    private Long appointmentId;

    @Column(name = "conversation_key", nullable = false, length = 80)
    private String conversationKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConversationStatus status = ConversationStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "last_message_at")
    private Instant lastMessageAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        conversationKey = buildKey(patientUserId, doctorUserId, appointmentId);
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
        conversationKey = buildKey(patientUserId, doctorUserId, appointmentId);
    }

    public static String buildKey(Long patientUserId, Long doctorUserId, Long appointmentId) {
        String appointmentPart = appointmentId == null ? "NONE" : appointmentId.toString();
        return patientUserId + ":" + doctorUserId + ":" + appointmentPart;
    }

    public boolean isParticipant(Long userId) {
        return userId != null && (userId.equals(patientUserId) || userId.equals(doctorUserId));
    }

    public boolean isActive() {
        return status == ConversationStatus.ACTIVE;
    }

    public Long getId() {
        return id;
    }

    public Long getPatientUserId() {
        return patientUserId;
    }

    public void setPatientUserId(Long patientUserId) {
        this.patientUserId = patientUserId;
    }

    public Long getDoctorUserId() {
        return doctorUserId;
    }

    public void setDoctorUserId(Long doctorUserId) {
        this.doctorUserId = doctorUserId;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getConversationKey() {
        return conversationKey;
    }

    public ConversationStatus getStatus() {
        return status;
    }

    public void setStatus(ConversationStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(Instant lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }
}
