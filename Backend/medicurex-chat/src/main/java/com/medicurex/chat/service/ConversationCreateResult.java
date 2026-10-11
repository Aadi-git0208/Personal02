package com.medicurex.chat.service;

import com.medicurex.chat.dto.ConversationResponse;

public record ConversationCreateResult(ConversationResponse conversation, boolean created) {
}
