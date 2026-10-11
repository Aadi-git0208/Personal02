package com.medicurex.appointment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicurex.appointment.client.ApprovedDoctorDirectory;
import com.medicurex.appointment.client.ProfileDoctorClient;
import com.medicurex.appointment.repository.AppointmentRepository;
import com.medicurex.appointment.support.TestJwtFactory;
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
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AppointmentApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @MockitoBean
    private ProfileDoctorClient profileDoctorClient;

    @BeforeEach
    void setUp() {
        appointmentRepository.deleteAll();
        when(profileDoctorClient.getApprovedDoctor(5L)).thenReturn(approvedDoctor(5L, 22L));
    }

    @Test
    void patientCanCreateAppointment() throws Exception {
        String token = bearer(TestJwtFactory.token(11L, "patient@test.local", "patient"));

        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", token)
                        .header("X-User-Id", "999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson(5L, "2099-06-01", "10:00:00", "10:30:00")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.patientUserId").value(11))
                .andExpect(jsonPath("$.doctorUserId").value(22))
                .andExpect(jsonPath("$.doctorProfileId").value(5))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void missingJwtIsRejected() throws Exception {
        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson(5L, "2099-06-01", "10:00:00", "10:30:00")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidJwtIsRejected() throws Exception {
        mockMvc.perform(get("/api/appointments/patient/me").header("Authorization", "Bearer not-a-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void expiredJwtIsRejected() throws Exception {
        mockMvc.perform(get("/api/appointments/patient/me")
                        .header("Authorization", bearer(TestJwtFactory.expiredToken(11L, "patient@test.local", "patient"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void patientCannotReadAnotherPatientsAppointment() throws Exception {
        long appointmentId = createAppointment(11L);
        String otherPatient = bearer(TestJwtFactory.token(12L, "other@test.local", "patient"));

        mockMvc.perform(get("/api/appointments/" + appointmentId).header("Authorization", otherPatient))
                .andExpect(status().isForbidden());
    }

    @Test
    void doctorCannotConfirmAnotherDoctorsAppointment() throws Exception {
        long appointmentId = createAppointment(11L);
        String otherDoctor = bearer(TestJwtFactory.token(99L, "otherdoc@test.local", "doctor"));

        mockMvc.perform(patch("/api/appointments/" + appointmentId + "/confirm").header("Authorization", otherDoctor))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanReadAnyAppointment() throws Exception {
        long appointmentId = createAppointment(11L);
        String admin = bearer(TestJwtFactory.token(1L, "admin@test.local", "admin"));

        mockMvc.perform(get("/api/appointments/admin/" + appointmentId).header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(appointmentId));
    }

    @Test
    void invalidTimeRangeIsRejected() throws Exception {
        String token = bearer(TestJwtFactory.token(11L, "patient@test.local", "patient"));

        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson(5L, "2099-06-01", "11:00:00", "10:00:00")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void overlappingDoctorBookingIsRejected() throws Exception {
        createAppointment(11L);
        String otherPatient = bearer(TestJwtFactory.token(12L, "other@test.local", "patient"));

        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", otherPatient)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson(5L, "2099-06-01", "10:15:00", "10:45:00")))
                .andExpect(status().isConflict());
    }

    @Test
    void cancelledAppointmentDoesNotBlockSlot() throws Exception {
        long appointmentId = createAppointment(11L);
        String patient = bearer(TestJwtFactory.token(11L, "patient@test.local", "patient"));

        mockMvc.perform(patch("/api/appointments/" + appointmentId + "/cancel")
                        .header("Authorization", patient)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Change of plans\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", patient)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson(5L, "2099-06-01", "10:00:00", "10:30:00")))
                .andExpect(status().isCreated());
    }

    @Test
    void doctorCanConfirmAndComplete() throws Exception {
        long appointmentId = createAppointment(11L);
        String doctor = bearer(TestJwtFactory.token(22L, "doctor@test.local", "doctor"));

        mockMvc.perform(patch("/api/appointments/" + appointmentId + "/confirm").header("Authorization", doctor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        mockMvc.perform(patch("/api/appointments/" + appointmentId + "/complete").header("Authorization", doctor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        mockMvc.perform(patch("/api/appointments/" + appointmentId + "/cancel").header("Authorization", doctor))
                .andExpect(status().isForbidden());
    }

    @Test
    void cannotCompletePendingAppointment() throws Exception {
        long appointmentId = createAppointment(11L);
        String doctor = bearer(TestJwtFactory.token(22L, "doctor@test.local", "doctor"));

        mockMvc.perform(patch("/api/appointments/" + appointmentId + "/complete").header("Authorization", doctor))
                .andExpect(status().isConflict());
    }

    @Test
    void unapprovedDoctorIsRejected() throws Exception {
        when(profileDoctorClient.getApprovedDoctor(8L)).thenThrow(notFound());
        String token = bearer(TestJwtFactory.token(11L, "patient@test.local", "patient"));

        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson(8L, "2099-06-01", "10:00:00", "10:30:00")))
                .andExpect(status().isNotFound());
    }

    @Test
    void profileServiceOutageFailsSafe() throws Exception {
        when(profileDoctorClient.getApprovedDoctor(anyLong())).thenThrow(new RuntimeException("connection refused"));
        String token = bearer(TestJwtFactory.token(11L, "patient@test.local", "patient"));

        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson(5L, "2099-06-01", "10:00:00", "10:30:00")))
                .andExpect(status().isBadGateway());
    }

    private long createAppointment(long patientId) throws Exception {
        String token = bearer(TestJwtFactory.token(patientId, "patient" + patientId + "@test.local", "patient"));
        MvcResult result = mockMvc.perform(post("/api/appointments")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson(5L, "2099-06-01", "10:00:00", "10:30:00")))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.get("id").asLong();
    }

    private ApprovedDoctorDirectory approvedDoctor(long profileId, long authUserId) {
        ApprovedDoctorDirectory doctor = new ApprovedDoctorDirectory();
        doctor.setId(profileId);
        doctor.setAuthUserId(authUserId);
        doctor.setFullName("Dr Sharma");
        doctor.setSpecialization("Cardiologist");
        doctor.setAvailabilitySlots(List.of("10:00 AM", "02:00 PM"));
        return doctor;
    }

    private FeignException notFound() {
        Request request = Request.create(
                Request.HttpMethod.GET,
                "/api/profiles/doctor/directory/8",
                Map.of(),
                null,
                StandardCharsets.UTF_8,
                new RequestTemplate()
        );
        Response response = Response.builder()
                .status(404)
                .reason("Not Found")
                .request(request)
                .headers(Map.of())
                .build();
        return FeignException.errorStatus("getApprovedDoctor", response);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String createJson(long doctorProfileId, String date, String start, String end) {
        return """
                {
                  "doctorProfileId": %d,
                  "appointmentDate": "%s",
                  "startTime": "%s",
                  "endTime": "%s",
                  "slotLabel": "10:00 AM",
                  "reason": "Annual checkup",
                  "notes": "Prefer morning"
                }
                """.formatted(doctorProfileId, date, start, end);
    }
}
