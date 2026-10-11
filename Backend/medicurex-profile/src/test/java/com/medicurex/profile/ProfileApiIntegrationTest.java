package com.medicurex.profile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicurex.profile.model.PatientProfile;
import com.medicurex.profile.repository.DoctorProfileRepository;
import com.medicurex.profile.repository.PatientProfileRepository;
import com.medicurex.profile.support.TestJwtFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProfileApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PatientProfileRepository patientProfileRepository;

    @Autowired
    private DoctorProfileRepository doctorProfileRepository;

    @BeforeEach
    void clean() {
        doctorProfileRepository.deleteAll();
        patientProfileRepository.deleteAll();
    }

    @Test
    void patientCanCreateAndUpdateOwnProfile() throws Exception {
        String token = bearer(TestJwtFactory.token(11L, "patient@test.local", "patient"));

        mockMvc.perform(put("/api/profiles/patient/me")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patientJson("Rahul Patient", "9876543210")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.authUserId").value(11))
                .andExpect(jsonPath("$.fullName").value("Rahul Patient"));

        mockMvc.perform(put("/api/profiles/patient/me")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patientJson("Rahul Updated", "9876543210")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Rahul Updated"));

        mockMvc.perform(get("/api/profiles/patient/me").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authUserId").value(11));

        assertThat(patientProfileRepository.count()).isEqualTo(1);
    }

    @Test
    void duplicatePatientAuthUserIsPrevented() {
        PatientProfile first = new PatientProfile();
        first.setAuthUserId(11L);
        first.setFullName("Rahul");
        first.setPhoneNumber("9876543210");
        first.setDateOfBirth(LocalDate.of(1995, 1, 1));
        patientProfileRepository.saveAndFlush(first);

        PatientProfile duplicate = new PatientProfile();
        duplicate.setAuthUserId(11L);
        duplicate.setFullName("Copy");
        duplicate.setPhoneNumber("9999999999");
        duplicate.setDateOfBirth(LocalDate.of(1996, 1, 1));

        assertThatThrownBy(() -> patientProfileRepository.saveAndFlush(duplicate))
                .isInstanceOf(Exception.class);
        assertThat(patientProfileRepository.count()).isEqualTo(1);
    }

    @Test
    void patientCannotAccessDoctorProfile() throws Exception {
        String token = bearer(TestJwtFactory.token(11L, "patient@test.local", "patient"));

        mockMvc.perform(get("/api/profiles/doctor/me").header("Authorization", token))
                .andExpect(status().isForbidden());
    }

    @Test
    void missingJwtIsRejected() throws Exception {
        mockMvc.perform(get("/api/profiles/patient/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidJwtIsRejected() throws Exception {
        mockMvc.perform(get("/api/profiles/patient/me").header("Authorization", "Bearer not-a-valid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid or expired token"));
    }

    @Test
    void expiredJwtIsRejected() throws Exception {
        String token = bearer(TestJwtFactory.expiredToken(11L, "patient@test.local", "patient"));

        mockMvc.perform(get("/api/profiles/patient/me").header("Authorization", token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Token has expired"));
    }

    @Test
    void spoofedUserIdHeaderIsIgnored() throws Exception {
        String token = bearer(TestJwtFactory.token(11L, "patient@test.local", "patient"));

        mockMvc.perform(put("/api/profiles/patient/me")
                        .header("Authorization", token)
                        .header("X-User-Id", "999")
                        .header("X-Role", "admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patientJson("Rahul Patient", "9876543210")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.authUserId").value(11));
    }

    @Test
    void invalidPhoneFailsValidation() throws Exception {
        String token = bearer(TestJwtFactory.token(11L, "patient@test.local", "patient"));

        mockMvc.perform(put("/api/profiles/patient/me")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patientJson("Rahul Patient", "123")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.phoneNumber").exists());
    }

    @Test
    void publicDirectoryHidesPendingDoctorsAndPrivateFields() throws Exception {
        String doctorToken = bearer(TestJwtFactory.token(22L, "doctor@test.local", "doctor"));
        String adminToken = bearer(TestJwtFactory.token(1L, "admin@test.local", "admin"));

        MvcResult created = mockMvc.perform(put("/api/profiles/doctor/me")
                        .header("Authorization", doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(doctorJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.approvalStatus").value("PENDING"))
                .andExpect(jsonPath("$.phoneNumber").value("9123456789"))
                .andReturn();

        JsonNode createdBody = objectMapper.readTree(created.getResponse().getContentAsString());
        long profileId = createdBody.get("id").asLong();

        mockMvc.perform(get("/api/profiles/doctor/directory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        mockMvc.perform(get("/api/profiles/doctor/directory/" + profileId))
                .andExpect(status().isNotFound());

        mockMvc.perform(patch("/api/profiles/admin/22/approval")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"APPROVED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approvalStatus").value("APPROVED"));

        mockMvc.perform(get("/api/profiles/doctor/directory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(profileId))
                .andExpect(jsonPath("$[0].fullName").value("Dr Sharma"))
                .andExpect(jsonPath("$[0].phoneNumber").doesNotExist())
                .andExpect(jsonPath("$[0].authUserId").value(22))
                .andExpect(jsonPath("$[0].approvalStatus").doesNotExist());

        mockMvc.perform(get("/api/profiles/doctor/directory/" + profileId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clinicAddress").value("City Hospital"))
                .andExpect(jsonPath("$.phoneNumber").doesNotExist());
    }

    @Test
    void doctorCannotApproveOwnProfile() throws Exception {
        String doctorToken = bearer(TestJwtFactory.token(22L, "doctor@test.local", "doctor"));

        mockMvc.perform(put("/api/profiles/doctor/me")
                        .header("Authorization", doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(doctorJson()))
                .andExpect(status().isCreated());

        mockMvc.perform(patch("/api/profiles/admin/22/approval")
                        .header("Authorization", doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"APPROVED\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanReadProfiles() throws Exception {
        String patientToken = bearer(TestJwtFactory.token(11L, "patient@test.local", "patient"));
        String adminToken = bearer(TestJwtFactory.token(1L, "admin@test.local", "admin"));

        mockMvc.perform(put("/api/profiles/patient/me")
                        .header("Authorization", patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patientJson("Rahul Patient", "9876543210")))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/profiles/admin").header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patients[0].authUserId").value(11));

        mockMvc.perform(get("/api/profiles/admin/11").header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patient.fullName").value("Rahul Patient"));
    }

    @Test
    void negativeConsultationFeeIsRejected() throws Exception {
        String doctorToken = bearer(TestJwtFactory.token(22L, "doctor@test.local", "doctor"));

        mockMvc.perform(put("/api/profiles/doctor/me")
                        .header("Authorization", doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Dr Sharma",
                                  "specialization": "Cardiologist",
                                  "qualifications": "MD",
                                  "experienceYears": 8,
                                  "consultationFee": -10,
                                  "clinicAddress": "City Hospital",
                                  "phoneNumber": "9123456789"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.consultationFee").exists());
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String patientJson(String name, String phone) {
        return """
                {
                  "fullName": "%s",
                  "phoneNumber": "%s",
                  "dateOfBirth": "1995-04-12",
                  "gender": "MALE",
                  "address": "Pune",
                  "bloodGroup": "O+"
                }
                """.formatted(name, phone);
    }

    private String doctorJson() {
        return """
                {
                  "fullName": "Dr Sharma",
                  "specialization": "Cardiologist",
                  "qualifications": "MD, DM",
                  "experienceYears": 8,
                  "consultationFee": 800.00,
                  "clinicAddress": "City Hospital",
                  "phoneNumber": "9123456789",
                  "biography": "Heart specialist",
                  "availabilitySlots": ["10:00 AM", "02:00 PM"]
                }
                """;
    }
}
