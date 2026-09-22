package com.apexcare.chat.service;

import com.apexcare.chat.client.AppointmentClient;
import com.apexcare.chat.client.AppointmentSnapshot;
import com.apexcare.chat.dto.CreateConversationRequest;
import com.apexcare.chat.entity.Conversation;
import com.apexcare.chat.exception.ApiException;
import com.apexcare.chat.repository.ConversationRepository;
import com.apexcare.chat.security.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConversationServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private AppointmentClient appointmentClient;

    private ConversationService conversationService;

    @BeforeEach
    void setUp() {
        conversationService = new ConversationService(conversationRepository, appointmentClient);
    }

    @Test
    void createsConversationFromAppointmentParticipants() {
        AuthenticatedUser patient = new AuthenticatedUser(11L, "rahul@gmail.com", "patient");
        CreateConversationRequest request = new CreateConversationRequest();
        request.setAppointmentId(1L);
        when(appointmentClient.getAppointment(1L, "Bearer token")).thenReturn(appointment(1L, "COMPLETED"));
        when(conversationRepository.findByAppointmentId(1L)).thenReturn(Optional.empty());
        when(conversationRepository.save(any(Conversation.class))).thenAnswer(invocation -> {
            Conversation conversation = invocation.getArgument(0);
            conversation.setId(4L);
            return conversation;
        });

        var response = conversationService.create(patient, "Bearer token", request);

        assertThat(response.getId()).isEqualTo(4L);
        assertThat(response.getPatientId()).isEqualTo(11L);
        assertThat(response.getDoctorId()).isEqualTo(21L);
        assertThat(response.getAppointmentId()).isEqualTo(1L);

        ArgumentCaptor<Conversation> captor = ArgumentCaptor.forClass(Conversation.class);
        verify(conversationRepository).save(captor.capture());
        assertThat(captor.getValue().getPatientId()).isEqualTo(11L);
        assertThat(captor.getValue().getDoctorId()).isEqualTo(21L);
    }

    @Test
    void reopensExistingConversationForTheSameAppointment() {
        AuthenticatedUser doctor = new AuthenticatedUser(21L, "doctor@gmail.com", "doctor");
        CreateConversationRequest request = new CreateConversationRequest();
        request.setAppointmentId(1L);
        Conversation existing = conversation(7L, 11L, 21L, 1L);
        when(appointmentClient.getAppointment(1L, "Bearer token")).thenReturn(appointment(1L, "CONFIRMED"));
        when(conversationRepository.findByAppointmentId(1L)).thenReturn(Optional.of(existing));

        var response = conversationService.create(doctor, "Bearer token", request);

        assertThat(response.getId()).isEqualTo(7L);
        verify(conversationRepository, never()).save(any());
    }

    @Test
    void rejectsCancelledAppointment() {
        AuthenticatedUser patient = new AuthenticatedUser(11L, "rahul@gmail.com", "patient");
        CreateConversationRequest request = new CreateConversationRequest();
        request.setAppointmentId(3L);
        when(appointmentClient.getAppointment(3L, "Bearer token")).thenReturn(appointment(3L, "CANCELLED"));

        assertThatThrownBy(() -> conversationService.create(patient, "Bearer token", request))
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> assertThat(((ApiException) exception).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void outsiderCannotOpenConversation() {
        AuthenticatedUser other = new AuthenticatedUser(99L, "other@gmail.com", "patient");
        CreateConversationRequest request = new CreateConversationRequest();
        request.setAppointmentId(1L);
        when(appointmentClient.getAppointment(1L, "Bearer token")).thenReturn(appointment(1L, "PENDING"));

        assertThatThrownBy(() -> conversationService.create(other, "Bearer token", request))
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> assertThat(((ApiException) exception).getStatus()).isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void listsOnlyTheCurrentDoctorsConversations() {
        AuthenticatedUser doctor = new AuthenticatedUser(21L, "doctor@gmail.com", "doctor");
        when(conversationRepository.findByDoctorIdOrderByUpdatedAtDesc(21L))
                .thenReturn(List.of(conversation(1L, 11L, 21L, 1L)));

        var responses = conversationService.list(doctor);

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().getDoctorId()).isEqualTo(21L);
        verify(conversationRepository, never()).findByPatientIdOrderByUpdatedAtDesc(any());
    }

    private static AppointmentSnapshot appointment(Long id, String status) {
        AppointmentSnapshot snapshot = new AppointmentSnapshot();
        snapshot.setId(id);
        snapshot.setPatientId(11L);
        snapshot.setDoctorId(21L);
        snapshot.setStatus(status);
        return snapshot;
    }

    private static Conversation conversation(Long id, Long patientId, Long doctorId, Long appointmentId) {
        Conversation conversation = new Conversation();
        conversation.setId(id);
        conversation.setPatientId(patientId);
        conversation.setDoctorId(doctorId);
        conversation.setAppointmentId(appointmentId);
        return conversation;
    }
}
