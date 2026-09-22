package com.apexcare.appointment;

import com.apexcare.appointment.client.DoctorProfileSnapshot;
import com.apexcare.appointment.client.ProfileClient;
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

import java.time.LocalDate;

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
class AppointmentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProfileClient profileClient;

    private final LocalDate tomorrow = LocalDate.now().plusDays(1);

    @BeforeEach
    void stubDoctor() {
        DoctorProfileSnapshot doctor = new DoctorProfileSnapshot();
        doctor.setId(3L);
        doctor.setUserId(21L);
        doctor.setConsultationFee(500);
        doctor.setProfileCompleted(true);
        when(profileClient.getDoctor(eq(3L), any())).thenReturn(doctor);
    }

    @Test
    void unauthenticatedAccessIsRejected() throws Exception {
        mockMvc.perform(get("/api/appointments/patient/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void successfulBookingUsesJwtIdentityNotBodyPatientId() throws Exception {
        String patientToken = TestJwtFactory.token(11L, "rahul@gmail.com", "patient");

        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "patientId": 999,
                                  "doctorId": 3,
                                  "appointmentDate": "%s",
                                  "startTime": "10:00:00",
                                  "endTime": "10:30:00",
                                  "reason": "Checkup"
                                }
                                """.formatted(tomorrow)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.patientId").value(11))
                .andExpect(jsonPath("$.doctorId").value(21))
                .andExpect(jsonPath("$.consultationFee").value(500))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void overlappingAppointmentIsRejected() throws Exception {
        String patientToken = TestJwtFactory.token(11L, "rahul@gmail.com", "patient");
        book(patientToken, "10:00:00", "10:30:00");

        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "doctorId": 3,
                                  "appointmentDate": "%s",
                                  "startTime": "10:15:00",
                                  "endTime": "10:45:00"
                                }
                                """.formatted(tomorrow)))
                .andExpect(status().isConflict());
    }

    @Test
    void patientCannotReadAnotherPatientsAppointment() throws Exception {
        String ownerToken = TestJwtFactory.token(11L, "rahul@gmail.com", "patient");
        String otherToken = TestJwtFactory.token(12L, "other@gmail.com", "patient");
        long id = book(ownerToken, "11:00:00", "11:30:00");

        mockMvc.perform(get("/api/appointments/" + id)
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void doctorCanAcceptAndThenComplete() throws Exception {
        String patientToken = TestJwtFactory.token(11L, "rahul@gmail.com", "patient");
        String doctorToken = TestJwtFactory.token(21L, "doctor@gmail.com", "doctor");
        long id = book(patientToken, "12:00:00", "12:30:00");

        mockMvc.perform(patch("/api/appointments/" + id + "/accept")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        mockMvc.perform(patch("/api/appointments/" + id + "/complete")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void doctorCanRejectPendingAppointment() throws Exception {
        String patientToken = TestJwtFactory.token(11L, "rahul@gmail.com", "patient");
        String doctorToken = TestJwtFactory.token(21L, "doctor@gmail.com", "doctor");
        long id = book(patientToken, "13:00:00", "13:30:00");

        mockMvc.perform(patch("/api/appointments/" + id + "/reject")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void otherDoctorCannotAcceptAppointment() throws Exception {
        String patientToken = TestJwtFactory.token(11L, "rahul@gmail.com", "patient");
        String otherDoctor = TestJwtFactory.token(22L, "otherdoc@gmail.com", "doctor");
        long id = book(patientToken, "14:00:00", "14:30:00");

        mockMvc.perform(patch("/api/appointments/" + id + "/accept")
                        .header("Authorization", "Bearer " + otherDoctor))
                .andExpect(status().isForbidden());
    }

    @Test
    void patientCanCancelOwnPendingAppointment() throws Exception {
        String patientToken = TestJwtFactory.token(11L, "rahul@gmail.com", "patient");
        long id = book(patientToken, "15:00:00", "15:30:00");

        mockMvc.perform(patch("/api/appointments/" + id + "/cancel")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void doctorCannotCompletePendingAppointment() throws Exception {
        String patientToken = TestJwtFactory.token(11L, "rahul@gmail.com", "patient");
        String doctorToken = TestJwtFactory.token(21L, "doctor@gmail.com", "doctor");
        long id = book(patientToken, "16:00:00", "16:30:00");

        mockMvc.perform(patch("/api/appointments/" + id + "/complete")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isConflict());
    }

    @Test
    void adminCanListAppointments() throws Exception {
        String patientToken = TestJwtFactory.token(11L, "rahul@gmail.com", "patient");
        String adminToken = TestJwtFactory.token(1L, "admin@medicurex.local", "admin");
        book(patientToken, "17:00:00", "17:30:00");

        mockMvc.perform(get("/api/appointments")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].patientId").value(11));
    }

    private long book(String patientToken, String start, String end) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "doctorId": 3,
                                  "appointmentDate": "%s",
                                  "startTime": "%s",
                                  "endTime": "%s",
                                  "reason": "Checkup"
                                }
                                """.formatted(tomorrow, start, end)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asLong();
    }
}
