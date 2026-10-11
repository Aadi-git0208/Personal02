package com.medicurex.chat.dto;

import com.medicurex.chat.model.Conversation;
import com.medicurex.chat.model.ConversationStatus;

import java.time.Instant;

public class ConversationResponse {

    private Long id;
    private Long patientUserId;
    private Long doctorUserId;
    private Long appointmentId;
    private ConversationStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastMessageAt;

    public static ConversationResponse from(Conversation conversation) {
        ConversationResponse response = new ConversationResponse();
        response.id = conversation.getId();
        response.patientUserId = conversation.getPatientUserId();
        response.doctorUserId = conversation.getDoctorUserId();
        response.appointmentId = conversation.getAppointmentId();
        response.status = conversation.getStatus();
        response.createdAt = conversation.getCreatedAt();
        response.updatedAt = conversation.getUpdatedAt();
        response.lastMessageAt = conversation.getLastMessageAt();
        return response;
    }

    public Long getId() {
        return id;
    }

    public Long getPatientUserId() {
        return patientUserId;
    }

    public Long getDoctorUserId() {
        return doctorUserId;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public ConversationStatus getStatus() {
        return status;
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
}
