package com.apexcare.profile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProfileIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void unauthenticatedRequestsAreRejected() throws Exception {
        mockMvc.perform(get("/api/profiles/patient/me"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/profiles/doctors"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void patientCanUpsertAndReadOwnProfileIgnoringBodyUserId() throws Exception {
        String token = TestJwtFactory.token(11L, "rahul@gmail.com", "patient");

        mockMvc.perform(put("/api/profiles/patient/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 999,
                                  "fullName": "Rahul",
                                  "phone": "9999999999",
                                  "gender": "male",
                                  "dateOfBirth": "1998-04-12",
                                  "address": "Pune"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(11))
                .andExpect(jsonPath("$.fullName").value("Rahul"))
                .andExpect(jsonPath("$.profileCompleted").value(true));

        mockMvc.perform(get("/api/profiles/patient/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(11))
                .andExpect(jsonPath("$.address").value("Pune"));
    }

    @Test
    void patientCannotAccessDoctorProfile() throws Exception {
        String token = TestJwtFactory.token(11L, "rahul@gmail.com", "patient");

        mockMvc.perform(get("/api/profiles/doctor/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void doctorDirectoryAndAvailabilityFlow() throws Exception {
        String doctorToken = TestJwtFactory.token(21L, "doctor@gmail.com", "doctor");
        String patientToken = TestJwtFactory.token(11L, "rahul@gmail.com", "patient");
        String adminToken = TestJwtFactory.token(1L, "admin@medicurex.local", "admin");

        mockMvc.perform(put("/api/profiles/doctor/me")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Dr Sharma",
                                  "phone": "8888888888",
                                  "specialization": "Cardiology",
                                  "experience": 8,
                                  "consultationFee": 500,
                                  "profileImage": "https://example.com/doc.jpg"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(21))
                .andExpect(jsonPath("$.profileCompleted").value(true));

        MvcResult created = mockMvc.perform(post("/api/profiles/doctor/me/availability")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dayOfWeek": "MONDAY",
                                  "startTime": "10:00:00",
                                  "endTime": "12:00:00",
                                  "available": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dayOfWeek").value("MONDAY"))
                .andReturn();

        JsonNode availability = objectMapper.readTree(created.getResponse().getContentAsString());
        long availabilityId = availability.get("id").asLong();
        long doctorId = availability.get("doctorId").asLong();

        mockMvc.perform(get("/api/profiles/doctors")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fullName").value("Dr Sharma"))
                .andExpect(jsonPath("$[0].specialization").value("Cardiology"));

        mockMvc.perform(get("/api/profiles/doctors/" + doctorId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(21))
                .andExpect(jsonPath("$.availability[0].id").value(availabilityId));

        mockMvc.perform(put("/api/profiles/doctor/me/availability/" + availabilityId)
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dayOfWeek": "MONDAY",
                                  "startTime": "10:00:00",
                                  "endTime": "13:00:00",
                                  "available": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.endTime").value("13:00:00"));

        mockMvc.perform(delete("/api/profiles/doctor/me/availability/" + availabilityId)
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void doctorCannotReadAnotherPatientsProfileViaMe() throws Exception {
        String doctorToken = TestJwtFactory.token(21L, "doctor@gmail.com", "doctor");

        mockMvc.perform(get("/api/profiles/patient/me")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void negativeConsultationFeeIsRejected() throws Exception {
        String doctorToken = TestJwtFactory.token(21L, "doctor@gmail.com", "doctor");

        mockMvc.perform(put("/api/profiles/doctor/me")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Dr Sharma",
                                  "specialization": "Cardiology",
                                  "experience": 8,
                                  "consultationFee": -10
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.consultationFee").exists());
    }

    @Test
    void healthIsPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }
}
