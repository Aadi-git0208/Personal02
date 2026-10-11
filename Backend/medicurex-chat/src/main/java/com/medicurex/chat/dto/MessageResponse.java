package com.medicurex.chat.dto;

import com.medicurex.chat.model.Message;

import java.time.Instant;

public class MessageResponse {

    private Long id;
    private Long conversationId;
    private Long senderUserId;
    private String body;
    private Instant createdAt;
    private Instant readAt;
    private boolean read;

    public static MessageResponse from(Message message) {
        MessageResponse response = new MessageResponse();
        response.id = message.getId();
        response.conversationId = message.getConversation().getId();
        response.senderUserId = message.getSenderUserId();
        response.body = message.getBody();
        response.createdAt = message.getCreatedAt();
        response.readAt = message.getReadAt();
        response.read = message.isRead();
        return response;
    }

    public Long getId() {
        return id;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public Long getSenderUserId() {
        return senderUserId;
    }

    public String getBody() {
        return body;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getReadAt() {
        return readAt;
    }

    public boolean isRead() {
        return read;
    }
}
