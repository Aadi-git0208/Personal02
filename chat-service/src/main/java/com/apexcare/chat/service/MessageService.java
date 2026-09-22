package com.apexcare.chat.service;

import com.apexcare.chat.dto.MessageResponse;
import com.apexcare.chat.dto.SendMessageRequest;
import com.apexcare.chat.entity.Conversation;
import com.apexcare.chat.entity.Message;
import com.apexcare.chat.exception.ApiException;
import com.apexcare.chat.realtime.ChatRealtimeNotifier;
import com.apexcare.chat.repository.ConversationRepository;
import com.apexcare.chat.repository.MessageRepository;
import com.apexcare.chat.security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MessageService {

    private final ConversationService conversationService;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ChatRealtimeNotifier realtimeNotifier;

    public MessageService(
            ConversationService conversationService,
            ConversationRepository conversationRepository,
            MessageRepository messageRepository,
            ChatRealtimeNotifier realtimeNotifier
    ) {
        this.conversationService = conversationService;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.realtimeNotifier = realtimeNotifier;
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> list(AuthenticatedUser user, Long conversationId) {
        Conversation conversation = conversationService.requireVisible(user, conversationId);
        return messageRepository.findByConversationOrderBySentAtAsc(conversation)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public MessageResponse send(AuthenticatedUser user, Long conversationId, SendMessageRequest request) {
        Conversation conversation = conversationService.requireParticipant(user, conversationId);
        Message message = new Message();
        message.setConversation(conversation);
        message.setSenderId(user.getUserId());
        message.setContent(request.getContent().trim());
        message.setRead(false);
        Message saved = messageRepository.save(message);
        conversation.touch();
        conversationRepository.save(conversation);

        MessageResponse response = toResponse(saved);
        realtimeNotifier.messageCreated(response);
        return response;
    }

    @Transactional
    public MessageResponse markRead(AuthenticatedUser user, Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Message not found"));
        conversationService.assertCanAccess(user, message.getConversation(), true);
        if (message.getSenderId().equals(user.getUserId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "A sender cannot mark their own message as read");
        }
        message.setRead(true);
        return toResponse(messageRepository.save(message));
    }

    MessageResponse toResponse(Message message) {
        MessageResponse response = new MessageResponse();
        response.setId(message.getId());
        response.setConversationId(message.getConversation().getId());
        response.setSenderId(message.getSenderId());
        response.setContent(message.getContent());
        response.setSentAt(message.getSentAt());
        response.setRead(message.isRead());
        return response;
    }
}
