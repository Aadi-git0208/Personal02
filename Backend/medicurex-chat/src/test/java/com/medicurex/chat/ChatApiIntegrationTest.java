package com.medicurex.chat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicurex.chat.client.AppointmentClient;
import com.medicurex.chat.client.AppointmentSnapshot;
import com.medicurex.chat.client.ApprovedDoctorDirectory;
import com.medicurex.chat.client.ProfileDoctorClient;
import com.medicurex.chat.repository.ConversationRepository;
import com.medicurex.chat.repository.MessageRepository;
import com.medicurex.chat.support.TestJwtFactory;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ChatApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @MockitoBean
    private ProfileDoctorClient profileDoctorClient;

    @MockitoBean
    private AppointmentClient appointmentClient;

    @BeforeEach
    void setUp() {
        messageRepository.deleteAll();
        conversationRepository.deleteAll();
        when(profileDoctorClient.getApprovedDoctor(5L)).thenReturn(approvedDoctor(5L, 22L));
        when(appointmentClient.getAppointment(90L)).thenReturn(appointment(90L, 11L, 22L, "CONFIRMED"));
    }

    @Test
    void missingJwtIsRejected() throws Exception {
        mockMvc.perform(get("/api/chat/conversations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidJwtIsRejected() throws Exception {
        mockMvc.perform(get("/api/chat/conversations").header("Authorization", "Bearer not-a-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void expiredJwtIsRejected() throws Exception {
        mockMvc.perform(get("/api/chat/conversations")
                        .header("Authorization", bearer(TestJwtFactory.expiredToken(11L, "patient@test.local", "patient"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void patientCanCreateConversationFromDirectory() throws Exception {
        String token = bearer(TestJwtFactory.token(11L, "patient@test.local", "patient"));

        mockMvc.perform(post("/api/chat/conversations")
                        .header("Authorization", token)
                        .header("X-User-Id", "999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"doctorProfileId\":5,\"patientUserId\":999,\"doctorUserId\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.patientUserId").value(11))
                .andExpect(jsonPath("$.doctorUserId").value(22))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void existingActiveConversationIsReturned() throws Exception {
        long conversationId = createGeneralConversation(11L);
        String token = bearer(TestJwtFactory.token(11L, "patient@test.local", "patient"));

        mockMvc.perform(post("/api/chat/conversations")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"doctorProfileId\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(conversationId));
    }

    @Test
    void doctorCannotStartConversationWithoutAppointment() throws Exception {
        String token = bearer(TestJwtFactory.token(22L, "doctor@test.local", "doctor"));

        mockMvc.perform(post("/api/chat/conversations")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"doctorProfileId\":5}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminCannotStartOrReadPrivateConversations() throws Exception {
        long conversationId = createGeneralConversation(11L);
        String admin = bearer(TestJwtFactory.token(1L, "admin@test.local", "admin"));

        mockMvc.perform(post("/api/chat/conversations")
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"doctorProfileId\":5}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/chat/conversations").header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));

        mockMvc.perform(get("/api/chat/conversations/" + conversationId).header("Authorization", admin))
                .andExpect(status().isForbidden());
    }

    @Test
    void messageSenderIsTakenFromJwt() throws Exception {
        long conversationId = createGeneralConversation(11L);
        String token = bearer(TestJwtFactory.token(11L, "patient@test.local", "patient"));

        mockMvc.perform(post("/api/chat/conversations/" + conversationId + "/messages")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"body\":\"Hello doctor\",\"senderUserId\":999}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.senderUserId").value(11))
                .andExpect(jsonPath("$.body").value("Hello doctor"))
                .andExpect(jsonPath("$.read").value(false));
    }

    @Test
    void nonParticipantCannotAccessConversation() throws Exception {
        long conversationId = createGeneralConversation(11L);
        String otherPatient = bearer(TestJwtFactory.token(12L, "other@test.local", "patient"));

        mockMvc.perform(get("/api/chat/conversations/" + conversationId).header("Authorization", otherPatient))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/chat/conversations/" + conversationId + "/messages")
                        .header("Authorization", otherPatient)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"body\":\"Intrusion\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/chat/conversations/" + conversationId + "/messages")
                        .header("Authorization", otherPatient))
                .andExpect(status().isForbidden());
    }

    @Test
    void emptyAndOversizedMessagesAreRejected() throws Exception {
        long conversationId = createGeneralConversation(11L);
        String token = bearer(TestJwtFactory.token(11L, "patient@test.local", "patient"));

        mockMvc.perform(post("/api/chat/conversations/" + conversationId + "/messages")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"body\":\"   \"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/chat/conversations/" + conversationId + "/messages")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"body\":\"" + "x".repeat(2001) + "\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void closedConversationRejectsMessagesUntilReopened() throws Exception {
        long conversationId = createGeneralConversation(11L);
        String patient = bearer(TestJwtFactory.token(11L, "patient@test.local", "patient"));

        mockMvc.perform(patch("/api/chat/conversations/" + conversationId + "/close").header("Authorization", patient))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));

        mockMvc.perform(post("/api/chat/conversations/" + conversationId + "/messages")
                        .header("Authorization", patient)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"body\":\"Still closed\"}"))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/chat/conversations")
                        .header("Authorization", patient)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"doctorProfileId\":5}"))
                .andExpect(status().isConflict());

        mockMvc.perform(patch("/api/chat/conversations/" + conversationId + "/reopen").header("Authorization", patient))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        mockMvc.perform(post("/api/chat/conversations/" + conversationId + "/messages")
                        .header("Authorization", patient)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"body\":\"Reopened\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void messagesArePaginatedInChronologicalOrder() throws Exception {
        long conversationId = createGeneralConversation(11L);
        String patient = bearer(TestJwtFactory.token(11L, "patient@test.local", "patient"));
        String doctor = bearer(TestJwtFactory.token(22L, "doctor@test.local", "doctor"));

        sendMessage(conversationId, patient, "First");
        sendMessage(conversationId, doctor, "Second");
        sendMessage(conversationId, patient, "Third");

        mockMvc.perform(get("/api/chat/conversations/" + conversationId + "/messages?page=0&size=2")
                        .header("Authorization", patient))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].body").value("First"))
                .andExpect(jsonPath("$.content[1].body").value("Second"))
                .andExpect(jsonPath("$.totalElements").value(3));

        mockMvc.perform(get("/api/chat/conversations/" + conversationId + "/messages?page=1&size=2")
                        .header("Authorization", patient))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].body").value("Third"));
    }

    @Test
    void participantsCanMarkMessagesRead() throws Exception {
        long conversationId = createGeneralConversation(11L);
        String patient = bearer(TestJwtFactory.token(11L, "patient@test.local", "patient"));
        String doctor = bearer(TestJwtFactory.token(22L, "doctor@test.local", "doctor"));
        sendMessage(conversationId, patient, "Hello");

        mockMvc.perform(patch("/api/chat/conversations/" + conversationId + "/read").header("Authorization", doctor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.markedRead").value(1));

        mockMvc.perform(get("/api/chat/conversations/" + conversationId + "/messages").header("Authorization", doctor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].read").value(true));
    }

    @Test
    void appointmentAssociationIsValidated() throws Exception {
        String patient = bearer(TestJwtFactory.token(11L, "patient@test.local", "patient"));

        mockMvc.perform(post("/api/chat/conversations")
                        .header("Authorization", patient)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"appointmentId\":90}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.appointmentId").value(90))
                .andExpect(jsonPath("$.patientUserId").value(11))
                .andExpect(jsonPath("$.doctorUserId").value(22));
    }

    @Test
    void nonParticipantAppointmentIsRejected() throws Exception {
        when(appointmentClient.getAppointment(91L)).thenThrow(feignStatus(403));
        String other = bearer(TestJwtFactory.token(12L, "other@test.local", "patient"));

        mockMvc.perform(post("/api/chat/conversations")
                        .header("Authorization", other)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"appointmentId\":91}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void cancelledAppointmentCannotStartConversation() throws Exception {
        when(appointmentClient.getAppointment(92L)).thenReturn(appointment(92L, 11L, 22L, "CANCELLED"));
        String patient = bearer(TestJwtFactory.token(11L, "patient@test.local", "patient"));

        mockMvc.perform(post("/api/chat/conversations")
                        .header("Authorization", patient)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"appointmentId\":92}"))
                .andExpect(status().isConflict());
    }

    @Test
    void appointmentOutageFailsSafe() throws Exception {
        when(appointmentClient.getAppointment(anyLong())).thenThrow(new RuntimeException("connection refused"));
        String patient = bearer(TestJwtFactory.token(11L, "patient@test.local", "patient"));

        mockMvc.perform(post("/api/chat/conversations")
                        .header("Authorization", patient)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"appointmentId\":90}"))
                .andExpect(status().isBadGateway());
    }

    @Test
    void unknownConversationIsNotFound() throws Exception {
        String patient = bearer(TestJwtFactory.token(11L, "patient@test.local", "patient"));

        mockMvc.perform(get("/api/chat/conversations/404").header("Authorization", patient))
                .andExpect(status().isNotFound());
    }

    private long createGeneralConversation(long patientId) throws Exception {
        String token = bearer(TestJwtFactory.token(patientId, "patient" + patientId + "@test.local", "patient"));
        MvcResult result = mockMvc.perform(post("/api/chat/conversations")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"doctorProfileId\":5}"))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.get("id").asLong();
    }

    private void sendMessage(long conversationId, String bearerToken, String text) throws Exception {
        mockMvc.perform(post("/api/chat/conversations/" + conversationId + "/messages")
                        .header("Authorization", bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"body\":\"" + text + "\"}"))
                .andExpect(status().isCreated());
    }

    private ApprovedDoctorDirectory approvedDoctor(long profileId, long authUserId) {
        ApprovedDoctorDirectory doctor = new ApprovedDoctorDirectory();
        doctor.setId(profileId);
        doctor.setAuthUserId(authUserId);
        doctor.setFullName("Dr Sharma");
        doctor.setSpecialization("Cardiologist");
        return doctor;
    }

    private AppointmentSnapshot appointment(long id, long patientUserId, long doctorUserId, String status) {
        AppointmentSnapshot snapshot = new AppointmentSnapshot();
        snapshot.setId(id);
        snapshot.setPatientUserId(patientUserId);
        snapshot.setDoctorUserId(doctorUserId);
        snapshot.setStatus(status);
        return snapshot;
    }

    private FeignException feignStatus(int status) {
        Request request = Request.create(
                Request.HttpMethod.GET,
                "/api/appointments/91",
                Map.of(),
                null,
                StandardCharsets.UTF_8,
                new RequestTemplate()
        );
        Response response = Response.builder()
                .status(status)
                .reason("Error")
                .request(request)
                .headers(Map.of())
                .build();
        return FeignException.errorStatus("getAppointment", response);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
