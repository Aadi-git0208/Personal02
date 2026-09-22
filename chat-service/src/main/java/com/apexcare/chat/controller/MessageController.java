package com.apexcare.chat.controller;

import com.apexcare.chat.dto.MessageResponse;
import com.apexcare.chat.security.AuthenticatedUser;
import com.apexcare.chat.service.MessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<MessageResponse> markRead(Authentication authentication, @PathVariable Long id) {
        return ResponseEntity.ok(messageService.markRead(AuthenticatedUser.from(authentication), id));
    }
}
