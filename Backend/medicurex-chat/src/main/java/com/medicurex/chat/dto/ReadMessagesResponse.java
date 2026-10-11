package com.medicurex.chat.dto;

public class ReadMessagesResponse {

    private Long conversationId;
    private int markedRead;

    public ReadMessagesResponse(Long conversationId, int markedRead) {
        this.conversationId = conversationId;
        this.markedRead = markedRead;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public int getMarkedRead() {
        return markedRead;
    }
}
