package com.apexcare.chat.service;

import com.apexcare.chat.client.AppointmentClient;
import com.apexcare.chat.client.AppointmentSnapshot;
import com.apexcare.chat.dto.ConversationResponse;
import com.apexcare.chat.dto.CreateConversationRequest;
import com.apexcare.chat.entity.Conversation;
import com.apexcare.chat.exception.ApiException;
import com.apexcare.chat.repository.ConversationRepository;
import com.apexcare.chat.security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final AppointmentClient appointmentClient;

    public ConversationService(
            ConversationRepository conversationRepository,
            AppointmentClient appointmentClient
    ) {
        this.conversationRepository = conversationRepository;
        this.appointmentClient = appointmentClient;
    }

    @Transactional
    public ConversationResponse create(AuthenticatedUser user, String authorization, CreateConversationRequest request) {
        AppointmentSnapshot appointment = appointmentClient.getAppointment(request.getAppointmentId(), authorization);
        if (!appointment.allowsChat()) {
            throw new ApiException(HttpStatus.CONFLICT, "Chat is only allowed for pending, confirmed or completed appointments");
        }

        boolean participant = user.getUserId().equals(appointment.getPatientId())
                || user.getUserId().equals(appointment.getDoctorId());
        if (!participant) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only open a conversation for your own appointment");
        }

        return conversationRepository.findByAppointmentId(appointment.getId())
                .map(this::toResponse)
                .orElseGet(() -> {
                    Conversation conversation = new Conversation();
                    conversation.setAppointmentId(appointment.getId());
                    conversation.setPatientId(appointment.getPatientId());
                    conversation.setDoctorId(appointment.getDoctorId());
                    return toResponse(conversationRepository.save(conversation));
                });
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> list(AuthenticatedUser user) {
        List<Conversation> conversations = switch (user.getRole()) {
            case "ADMIN" -> conversationRepository.findAllByOrderByUpdatedAtDesc();
            case "DOCTOR" -> conversationRepository.findByDoctorIdOrderByUpdatedAtDesc(user.getUserId());
            default -> conversationRepository.findByPatientIdOrderByUpdatedAtDesc(user.getUserId());
        };
        return conversations.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ConversationResponse get(AuthenticatedUser user, Long id) {
        return toResponse(requireVisible(user, id));
    }

    public Conversation requireVisible(AuthenticatedUser user, Long id) {
        Conversation conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Conversation not found"));
        assertCanAccess(user, conversation, false);
        return conversation;
    }

    public Conversation requireParticipant(AuthenticatedUser user, Long id) {
        Conversation conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Conversation not found"));
        assertCanAccess(user, conversation, true);
        return conversation;
    }

    public void assertCanAccess(AuthenticatedUser user, Conversation conversation, boolean write) {
        if ("ADMIN".equals(user.getRole())) {
            if (write) {
                throw new ApiException(HttpStatus.FORBIDDEN, "Admin access is read-only");
            }
            return;
        }
        boolean participant = user.getUserId().equals(conversation.getPatientId())
                || user.getUserId().equals(conversation.getDoctorId());
        if (!participant) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access denied");
        }
    }

    ConversationResponse toResponse(Conversation conversation) {
        ConversationResponse response = new ConversationResponse();
        response.setId(conversation.getId());
        response.setPatientId(conversation.getPatientId());
        response.setDoctorId(conversation.getDoctorId());
        response.setAppointmentId(conversation.getAppointmentId());
        response.setCreatedAt(conversation.getCreatedAt());
        response.setUpdatedAt(conversation.getUpdatedAt());
        return response;
    }
}
