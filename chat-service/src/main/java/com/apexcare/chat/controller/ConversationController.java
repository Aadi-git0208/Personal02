package com.apexcare.chat.controller;

import com.apexcare.chat.dto.ConversationResponse;
import com.apexcare.chat.dto.CreateConversationRequest;
import com.apexcare.chat.dto.MessageResponse;
import com.apexcare.chat.dto.SendMessageRequest;
import com.apexcare.chat.security.AuthenticatedUser;
import com.apexcare.chat.service.ConversationService;
import com.apexcare.chat.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chat/conversations")
public class ConversationController {

    private final ConversationService conversationService;
    private final MessageService messageService;

    public ConversationController(ConversationService conversationService, MessageService messageService) {
        this.conversationService = conversationService;
        this.messageService = messageService;
    }

    @PostMapping
    public ResponseEntity<ConversationResponse> create(
            Authentication authentication,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @Valid @RequestBody CreateConversationRequest request
    ) {
        AuthenticatedUser user = AuthenticatedUser.from(authentication);
        ConversationResponse created = conversationService.create(user, authorization, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<ConversationResponse>> list(Authentication authentication) {
        return ResponseEntity.ok(conversationService.list(AuthenticatedUser.from(authentication)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConversationResponse> get(Authentication authentication, @PathVariable Long id) {
        return ResponseEntity.ok(conversationService.get(AuthenticatedUser.from(authentication), id));
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<List<MessageResponse>> messages(Authentication authentication, @PathVariable Long id) {
        return ResponseEntity.ok(messageService.list(AuthenticatedUser.from(authentication), id));
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<MessageResponse> send(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody SendMessageRequest request
    ) {
        AuthenticatedUser user = AuthenticatedUser.from(authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(messageService.send(user, id, request));
    }
}
