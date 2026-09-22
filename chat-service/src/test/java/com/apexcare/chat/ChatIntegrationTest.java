package com.apexcare.chat;

import com.apexcare.chat.client.AppointmentClient;
import com.apexcare.chat.client.AppointmentSnapshot;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ChatIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AppointmentClient appointmentClient;

    @BeforeEach
    void stubAppointment() {
        when(appointmentClient.getAppointment(eq(1L), any())).thenReturn(appointment(1L, "COMPLETED"));
        when(appointmentClient.getAppointment(eq(2L), any())).thenReturn(appointment(2L, "REJECTED"));
    }

    @Test
    void unauthenticatedAccessIsRejected() throws Exception {
        mockMvc.perform(get("/api/chat/conversations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void patientAndDoctorCanOpenTheSameAppointmentConversation() throws Exception {
        String patientToken = TestJwtFactory.token(11L, "rahul@gmail.com", "patient");
        String doctorToken = TestJwtFactory.token(21L, "doctor@gmail.com", "doctor");

        long id = createConversation(patientToken, 1L);

        mockMvc.perform(post("/api/chat/conversations")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"appointmentId\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.patientId").value(11))
                .andExpect(jsonPath("$.doctorId").value(21));
    }

    @Test
    void rejectedAppointmentCannotOpenAConversation() throws Exception {
        String patientToken = TestJwtFactory.token(11L, "rahul@gmail.com", "patient");

        mockMvc.perform(post("/api/chat/conversations")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"appointmentId\":2}"))
                .andExpect(status().isConflict());
    }

    @Test
    void otherPatientCannotReadConversation() throws Exception {
        String ownerToken = TestJwtFactory.token(11L, "rahul@gmail.com", "patient");
        String otherToken = TestJwtFactory.token(12L, "other@gmail.com", "patient");
        long id = createConversation(ownerToken, 1L);

        mockMvc.perform(get("/api/chat/conversations/" + id)
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void otherDoctorCannotReadConversation() throws Exception {
        String ownerToken = TestJwtFactory.token(11L, "rahul@gmail.com", "patient");
        String otherDoctor = TestJwtFactory.token(22L, "otherdoc@gmail.com", "doctor");
        long id = createConversation(ownerToken, 1L);

        mockMvc.perform(get("/api/chat/conversations/" + id)
                        .header("Authorization", "Bearer " + otherDoctor))
                .andExpect(status().isForbidden());
    }

    @Test
    void patientAndDoctorCanExchangeMessagesAndMarkRead() throws Exception {
        String patientToken = TestJwtFactory.token(11L, "rahul@gmail.com", "patient");
        String doctorToken = TestJwtFactory.token(21L, "doctor@gmail.com", "doctor");
        long conversationId = createConversation(patientToken, 1L);

        MvcResult sent = mockMvc.perform(post("/api/chat/conversations/" + conversationId + "/messages")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Hello doctor\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.senderId").value(11))
                .andExpect(jsonPath("$.content").value("Hello doctor"))
                .andExpect(jsonPath("$.read").value(false))
                .andReturn();
        long messageId = objectMapper.readTree(sent.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/chat/conversations/" + conversationId + "/messages")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(messageId));

        mockMvc.perform(patch("/api/chat/messages/" + messageId + "/read")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true));
    }

    @Test
    void senderCannotMarkOwnMessageRead() throws Exception {
        String patientToken = TestJwtFactory.token(11L, "rahul@gmail.com", "patient");
        long conversationId = createConversation(patientToken, 1L);
        long messageId = sendMessage(patientToken, conversationId, "Note");

        mockMvc.perform(patch("/api/chat/messages/" + messageId + "/read")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanReadButCannotSend() throws Exception {
        String patientToken = TestJwtFactory.token(11L, "rahul@gmail.com", "patient");
        String adminToken = TestJwtFactory.token(1L, "admin@medicurex.local", "admin");
        long conversationId = createConversation(patientToken, 1L);
        sendMessage(patientToken, conversationId, "Hello");

        mockMvc.perform(get("/api/chat/conversations")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].patientId").value(11));

        mockMvc.perform(get("/api/chat/conversations/" + conversationId + "/messages")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Hello"));

        mockMvc.perform(post("/api/chat/conversations/" + conversationId + "/messages")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"admin note\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void blankMessageIsRejected() throws Exception {
        String patientToken = TestJwtFactory.token(11L, "rahul@gmail.com", "patient");
        long conversationId = createConversation(patientToken, 1L);

        mockMvc.perform(post("/api/chat/conversations/" + conversationId + "/messages")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"   \"}"))
                .andExpect(status().isBadRequest());
    }

    private long createConversation(String token, long appointmentId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/chat/conversations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"appointmentId\":" + appointmentId + "}"))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asLong();
    }

    private long sendMessage(String token, long conversationId, String content) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/chat/conversations/" + conversationId + "/messages")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"" + content + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private static AppointmentSnapshot appointment(Long id, String status) {
        AppointmentSnapshot snapshot = new AppointmentSnapshot();
        snapshot.setId(id);
        snapshot.setPatientId(11L);
        snapshot.setDoctorId(21L);
        snapshot.setStatus(status);
        return snapshot;
    }
}
