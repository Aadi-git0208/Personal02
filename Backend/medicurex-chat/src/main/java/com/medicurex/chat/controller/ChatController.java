package com.medicurex.chat.controller;

import com.medicurex.chat.dto.ConversationResponse;
import com.medicurex.chat.dto.CreateConversationRequest;
import com.medicurex.chat.dto.CreateMessageRequest;
import com.medicurex.chat.dto.MessageResponse;
import com.medicurex.chat.dto.ReadMessagesResponse;
import com.medicurex.chat.security.SecurityUtils;
import com.medicurex.chat.service.ChatService;
import com.medicurex.chat.service.ConversationCreateResult;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/conversations")
    public ResponseEntity<ConversationResponse> create(@Valid @RequestBody CreateConversationRequest request) {
        ConversationCreateResult result = chatService.createConversation(SecurityUtils.currentUser(), request);
        HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(result.conversation());
    }

    @GetMapping("/conversations")
    public Page<ConversationResponse> list(@PageableDefault(size = 20) Pageable pageable) {
        return chatService.listMine(SecurityUtils.currentUser(), pageable);
    }

    @GetMapping("/conversations/{conversationId}")
    public ConversationResponse get(@PathVariable Long conversationId) {
        return chatService.get(SecurityUtils.currentUser(), conversationId);
    }

    @PatchMapping("/conversations/{conversationId}/close")
    public ConversationResponse close(@PathVariable Long conversationId) {
        return chatService.close(SecurityUtils.currentUser(), conversationId);
    }

    @PatchMapping("/conversations/{conversationId}/reopen")
    public ConversationResponse reopen(@PathVariable Long conversationId) {
        return chatService.reopen(SecurityUtils.currentUser(), conversationId);
    }

    @PostMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<MessageResponse> send(
            @PathVariable Long conversationId,
            @Valid @RequestBody CreateMessageRequest request
    ) {
        MessageResponse message = chatService.sendMessage(SecurityUtils.currentUser(), conversationId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public Page<MessageResponse> messages(
            @PathVariable Long conversationId,
            @PageableDefault(size = 50) Pageable pageable
    ) {
        return chatService.listMessages(SecurityUtils.currentUser(), conversationId, pageable);
    }

    @PatchMapping("/conversations/{conversationId}/read")
    public ReadMessagesResponse markRead(@PathVariable Long conversationId) {
        return chatService.markRead(SecurityUtils.currentUser(), conversationId);
    }
}
