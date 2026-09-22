package com.apexcare.chat.service;

import com.apexcare.chat.dto.SendMessageRequest;
import com.apexcare.chat.entity.Conversation;
import com.apexcare.chat.entity.Message;
import com.apexcare.chat.exception.ApiException;
import com.apexcare.chat.realtime.ChatRealtimeNotifier;
import com.apexcare.chat.repository.ConversationRepository;
import com.apexcare.chat.repository.MessageRepository;
import com.apexcare.chat.security.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private ConversationService conversationService;

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ChatRealtimeNotifier realtimeNotifier;

    private MessageService messageService;

    @BeforeEach
    void setUp() {
        messageService = new MessageService(
                conversationService,
                conversationRepository,
                messageRepository,
                realtimeNotifier
        );
    }

    @Test
    void sendsMessageAsJwtUserAndNotifiesRealtimeHook() {
        AuthenticatedUser patient = new AuthenticatedUser(11L, "rahul@gmail.com", "patient");
        Conversation conversation = conversation();
        when(conversationService.requireParticipant(patient, 4L)).thenReturn(conversation);
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> {
            Message message = invocation.getArgument(0);
            message.setId(8L);
            message.setSentAt(Instant.parse("2026-09-21T18:00:00Z"));
            return message;
        });
        when(conversationRepository.save(conversation)).thenReturn(conversation);

        SendMessageRequest request = new SendMessageRequest();
        request.setContent("  Hello doctor  ");

        var response = messageService.send(patient, 4L, request);

        assertThat(response.getId()).isEqualTo(8L);
        assertThat(response.getSenderId()).isEqualTo(11L);
        assertThat(response.getContent()).isEqualTo("Hello doctor");
        assertThat(response.isRead()).isFalse();
        verify(realtimeNotifier).messageCreated(response);
        verify(conversationRepository).save(conversation);
    }

    @Test
    void recipientCanMarkMessageRead() {
        AuthenticatedUser doctor = new AuthenticatedUser(21L, "doctor@gmail.com", "doctor");
        Message message = message(8L, 11L, false);
        when(messageRepository.findById(8L)).thenReturn(Optional.of(message));
        when(messageRepository.save(message)).thenReturn(message);

        var response = messageService.markRead(doctor, 8L);

        assertThat(response.isRead()).isTrue();
        verify(conversationService).assertCanAccess(doctor, message.getConversation(), true);
    }

    @Test
    void senderCannotMarkOwnMessageRead() {
        AuthenticatedUser patient = new AuthenticatedUser(11L, "rahul@gmail.com", "patient");
        Message message = message(8L, 11L, false);
        when(messageRepository.findById(8L)).thenReturn(Optional.of(message));

        assertThatThrownBy(() -> messageService.markRead(patient, 8L))
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> assertThat(((ApiException) exception).getStatus()).isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void adminCannotWriteMessages() {
        AuthenticatedUser admin = new AuthenticatedUser(1L, "admin@medicurex.local", "admin");
        Conversation conversation = conversation();
        when(conversationService.requireParticipant(admin, 4L)).thenThrow(
                new ApiException(HttpStatus.FORBIDDEN, "Admin access is read-only")
        );

        SendMessageRequest request = new SendMessageRequest();
        request.setContent("admin note");

        assertThatThrownBy(() -> messageService.send(admin, 4L, request))
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> assertThat(((ApiException) exception).getStatus()).isEqualTo(HttpStatus.FORBIDDEN));
    }

    private static Conversation conversation() {
        Conversation conversation = new Conversation();
        conversation.setId(4L);
        conversation.setPatientId(11L);
        conversation.setDoctorId(21L);
        conversation.setAppointmentId(1L);
        return conversation;
    }

    private static Message message(Long id, Long senderId, boolean read) {
        Message message = new Message();
        message.setId(id);
        message.setConversation(conversation());
        message.setSenderId(senderId);
        message.setContent("Hello");
        message.setSentAt(Instant.parse("2026-09-21T18:00:00Z"));
        message.setRead(read);
        return message;
    }
}
