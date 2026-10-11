package com.medicurex.chat.service;

import com.medicurex.chat.client.AppointmentLookupService;
import com.medicurex.chat.client.AppointmentSnapshot;
import com.medicurex.chat.client.ApprovedDoctorDirectory;
import com.medicurex.chat.client.ProfileDirectoryService;
import com.medicurex.chat.dto.ConversationResponse;
import com.medicurex.chat.dto.CreateConversationRequest;
import com.medicurex.chat.dto.CreateMessageRequest;
import com.medicurex.chat.dto.MessageResponse;
import com.medicurex.chat.dto.ReadMessagesResponse;
import com.medicurex.chat.exception.ApiException;
import com.medicurex.chat.model.Conversation;
import com.medicurex.chat.model.ConversationStatus;
import com.medicurex.chat.model.Message;
import com.medicurex.chat.repository.ConversationRepository;
import com.medicurex.chat.repository.MessageRepository;
import com.medicurex.chat.security.AuthenticatedUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final AppointmentLookupService appointmentLookupService;
    private final ProfileDirectoryService profileDirectoryService;

    public ChatService(
            ConversationRepository conversationRepository,
            MessageRepository messageRepository,
            AppointmentLookupService appointmentLookupService,
            ProfileDirectoryService profileDirectoryService
    ) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.appointmentLookupService = appointmentLookupService;
        this.profileDirectoryService = profileDirectoryService;
    }

    @Transactional
    public ConversationCreateResult createConversation(AuthenticatedUser user, CreateConversationRequest request) {
        if (!user.isPatient() && !user.isDoctor()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only patients and doctors can start conversations");
        }

        ParticipantPair pair = resolveParticipants(user, request);
        String key = Conversation.buildKey(pair.patientUserId(), pair.doctorUserId(), pair.appointmentId());

        Optional<Conversation> existing = conversationRepository.lockByConversationKey(key);
        if (existing.isPresent()) {
            Conversation conversation = existing.get();
            if (conversation.getStatus() == ConversationStatus.CLOSED) {
                throw new ApiException(
                        HttpStatus.CONFLICT,
                        "This conversation is closed. Reopen it before sending new messages"
                );
            }
            return new ConversationCreateResult(ConversationResponse.from(conversation), false);
        }

        Conversation conversation = new Conversation();
        conversation.setPatientUserId(pair.patientUserId());
        conversation.setDoctorUserId(pair.doctorUserId());
        conversation.setAppointmentId(pair.appointmentId());
        conversation.setStatus(ConversationStatus.ACTIVE);
        Conversation saved = conversationRepository.save(conversation);
        return new ConversationCreateResult(ConversationResponse.from(saved), true);
    }

    @Transactional(readOnly = true)
    public Page<ConversationResponse> listMine(AuthenticatedUser user, Pageable pageable) {
        return conversationRepository
                .findByPatientUserIdOrDoctorUserIdOrderByLastMessageAtDescUpdatedAtDesc(
                        user.getUserId(),
                        user.getUserId(),
                        pageable
                )
                .map(ConversationResponse::from);
    }

    @Transactional(readOnly = true)
    public ConversationResponse get(AuthenticatedUser user, Long conversationId) {
        return ConversationResponse.from(requireParticipant(user, conversationId));
    }

    @Transactional
    public ConversationResponse close(AuthenticatedUser user, Long conversationId) {
        Conversation conversation = requireParticipant(user, conversationId);
        conversation.setStatus(ConversationStatus.CLOSED);
        return ConversationResponse.from(conversationRepository.save(conversation));
    }

    @Transactional
    public ConversationResponse reopen(AuthenticatedUser user, Long conversationId) {
        Conversation conversation = requireParticipant(user, conversationId);
        conversation.setStatus(ConversationStatus.ACTIVE);
        return ConversationResponse.from(conversationRepository.save(conversation));
    }

    @Transactional
    public MessageResponse sendMessage(AuthenticatedUser user, Long conversationId, CreateMessageRequest request) {
        Conversation conversation = requireParticipant(user, conversationId);
        if (!conversation.isActive()) {
            throw new ApiException(HttpStatus.CONFLICT, "Cannot send messages to a closed conversation");
        }

        String body = request.getBody() == null ? "" : request.getBody().trim();
        if (body.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Message text is required");
        }
        if (body.length() > 2000) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Message must be at most 2000 characters");
        }

        Message message = new Message();
        message.setConversation(conversation);
        message.setSenderUserId(user.getUserId());
        message.setBody(body);

        Instant now = Instant.now();
        conversation.setLastMessageAt(now);
        conversationRepository.save(conversation);
        return MessageResponse.from(messageRepository.save(message));
    }

    @Transactional(readOnly = true)
    public Page<MessageResponse> listMessages(AuthenticatedUser user, Long conversationId, Pageable pageable) {
        requireParticipant(user, conversationId);
        Pageable chronological = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.ASC, "createdAt")
        );
        return messageRepository
                .findByConversationIdOrderByCreatedAtAsc(conversationId, chronological)
                .map(MessageResponse::from);
    }

    @Transactional
    public ReadMessagesResponse markRead(AuthenticatedUser user, Long conversationId) {
        requireParticipant(user, conversationId);
        int marked = messageRepository.markOtherPartyMessagesRead(conversationId, user.getUserId(), Instant.now());
        return new ReadMessagesResponse(conversationId, marked);
    }

    private ParticipantPair resolveParticipants(AuthenticatedUser user, CreateConversationRequest request) {
        if (request.getAppointmentId() != null) {
            AppointmentSnapshot appointment = appointmentLookupService.requireParticipantAppointment(
                    request.getAppointmentId(),
                    user.getUserId()
            );
            return new ParticipantPair(
                    appointment.getPatientUserId(),
                    appointment.getDoctorUserId(),
                    appointment.getId()
            );
        }

        if (!user.isPatient()) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Doctors must provide appointmentId to start a conversation"
            );
        }

        ApprovedDoctorDirectory doctor = profileDirectoryService.requireApprovedDoctor(request.getDoctorProfileId());
        if (doctor.getAuthUserId().equals(user.getUserId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "You cannot start a conversation with yourself");
        }
        return new ParticipantPair(user.getUserId(), doctor.getAuthUserId(), null);
    }

    private Conversation requireParticipant(AuthenticatedUser user, Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Conversation not found"));
        if (!conversation.isParticipant(user.getUserId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return conversation;
    }

    private record ParticipantPair(Long patientUserId, Long doctorUserId, Long appointmentId) {
    }
}
