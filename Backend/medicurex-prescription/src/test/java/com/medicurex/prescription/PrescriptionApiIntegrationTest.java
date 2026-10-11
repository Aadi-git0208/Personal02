package com.medicurex.prescription;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicurex.prescription.client.AppointmentClient;
import com.medicurex.prescription.client.AppointmentSnapshot;
import com.medicurex.prescription.model.PrescriptionStatus;
import com.medicurex.prescription.repository.PrescriptionRepository;
import com.medicurex.prescription.support.TestJwtFactory;
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

import static org.assertj.core.api.Assertions.assertThat;
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
class PrescriptionApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @MockitoBean
    private AppointmentClient appointmentClient;

    @BeforeEach
    void setUp() {
        prescriptionRepository.deleteAll();
        when(appointmentClient.getAppointment(90L)).thenReturn(appointment(90L, 11L, 22L, "COMPLETED"));
    }

    @Test
    void missingJwtIsRejected() throws Exception {
        mockMvc.perform(get("/api/prescriptions/doctor/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidJwtIsRejected() throws Exception {
        mockMvc.perform(get("/api/prescriptions/patient/me").header("Authorization", "Bearer not-a-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void expiredJwtIsRejected() throws Exception {
        mockMvc.perform(get("/api/prescriptions/patient/me")
                        .header("Authorization", bearer(TestJwtFactory.expiredToken(11L, "patient@test.local", "patient"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void doctorCanCreatePrescriptionFromValidatedAppointment() throws Exception {
        String doctor = bearer(TestJwtFactory.token(22L, "doctor@test.local", "doctor"));

        mockMvc.perform(post("/api/prescriptions")
                        .header("Authorization", doctor)
                        .header("X-User-Id", "999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson(90L, 1L, 99L)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.patientUserId").value(11))
                .andExpect(jsonPath("$.doctorUserId").value(22))
                .andExpect(jsonPath("$.appointmentId").value(90))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.diagnosis").value("Hypertension"))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].medicineName").value("Amlodipine"))
                .andExpect(jsonPath("$.items[0].dosage").value("5 mg"))
                .andExpect(jsonPath("$.items[0].quantity").value(14));

        Long savedId = prescriptionRepository.findAll().getFirst().getId();
        assertThat(prescriptionRepository.findWithItemsById(savedId).orElseThrow().getItems()).hasSize(1);
    }

    @Test
    void emptyOrInvalidItemsAreRejected() throws Exception {
        String doctor = bearer(TestJwtFactory.token(22L, "doctor@test.local", "doctor"));

        mockMvc.perform(post("/api/prescriptions")
                        .header("Authorization", doctor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "appointmentId": 90,
                                  "diagnosis": "Hypertension",
                                  "items": []
                                }
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/prescriptions")
                        .header("Authorization", doctor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "appointmentId": 90,
                                  "diagnosis": "Hypertension",
                                  "items": [{
                                    "medicineName": "   ",
                                    "dosage": "5 mg",
                                    "frequency": "once daily",
                                    "duration": "14 days"
                                  }]
                                }
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/prescriptions")
                        .header("Authorization", doctor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "appointmentId": 90,
                                  "diagnosis": "Hypertension",
                                  "items": [{
                                    "medicineName": "Amlodipine",
                                    "dosage": "5 mg",
                                    "frequency": "once daily",
                                    "duration": "14 days",
                                    "quantity": 0
                                  }]
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void otherDoctorCannotPrescribeForAppointment() throws Exception {
        String otherDoctor = bearer(TestJwtFactory.token(33L, "otherdoc@test.local", "doctor"));

        mockMvc.perform(post("/api/prescriptions")
                        .header("Authorization", otherDoctor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson(90L, 11L, 33L)))
                .andExpect(status().isForbidden());
    }

    @Test
    void patientCannotCreatePrescription() throws Exception {
        String patient = bearer(TestJwtFactory.token(11L, "patient@test.local", "patient"));

        mockMvc.perform(post("/api/prescriptions")
                        .header("Authorization", patient)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson(90L, 11L, 22L)))
                .andExpect(status().isForbidden());
    }

    @Test
    void patientCanReadOwnPrescriptionAndNotAnotherPatients() throws Exception {
        long prescriptionId = createPrescription();
        String patient = bearer(TestJwtFactory.token(11L, "patient@test.local", "patient"));
        String otherPatient = bearer(TestJwtFactory.token(12L, "other@test.local", "patient"));

        mockMvc.perform(get("/api/prescriptions/patient/me/" + prescriptionId).header("Authorization", patient))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(prescriptionId))
                .andExpect(jsonPath("$.items[0].medicineName").value("Amlodipine"));

        mockMvc.perform(get("/api/prescriptions/patient/me").header("Authorization", patient))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));

        mockMvc.perform(get("/api/prescriptions/patient/me/" + prescriptionId).header("Authorization", otherPatient))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/prescriptions/" + prescriptionId).header("Authorization", otherPatient))
                .andExpect(status().isForbidden());
    }

    @Test
    void otherDoctorCannotViewOrCancel() throws Exception {
        long prescriptionId = createPrescription();
        String otherDoctor = bearer(TestJwtFactory.token(33L, "otherdoc@test.local", "doctor"));

        mockMvc.perform(get("/api/prescriptions/" + prescriptionId).header("Authorization", otherDoctor))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/prescriptions/" + prescriptionId + "/cancel").header("Authorization", otherDoctor))
                .andExpect(status().isForbidden());
    }

    @Test
    void doctorCanCancelAndAuditTrailIsPreserved() throws Exception {
        long prescriptionId = createPrescription();
        String doctor = bearer(TestJwtFactory.token(22L, "doctor@test.local", "doctor"));

        mockMvc.perform(patch("/api/prescriptions/" + prescriptionId + "/cancel")
                        .header("Authorization", doctor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Dosage correction\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.cancellationReason").value("Dosage correction"))
                .andExpect(jsonPath("$.items", hasSize(1)));

        assertThat(prescriptionRepository.findWithItemsById(prescriptionId)).isPresent();
        assertThat(prescriptionRepository.findWithItemsById(prescriptionId).orElseThrow().getStatus())
                .isEqualTo(PrescriptionStatus.CANCELLED);
        assertThat(prescriptionRepository.findWithItemsById(prescriptionId).orElseThrow().getItems()).hasSize(1);

        mockMvc.perform(patch("/api/prescriptions/" + prescriptionId + "/cancel").header("Authorization", doctor))
                .andExpect(status().isConflict());
    }

    @Test
    void patientCannotCancelPrescription() throws Exception {
        long prescriptionId = createPrescription();
        String patient = bearer(TestJwtFactory.token(11L, "patient@test.local", "patient"));

        mockMvc.perform(patch("/api/prescriptions/" + prescriptionId + "/cancel").header("Authorization", patient))
                .andExpect(status().isForbidden());
    }

    @Test
    void duplicateActivePrescriptionForAppointmentIsRejected() throws Exception {
        createPrescription();
        String doctor = bearer(TestJwtFactory.token(22L, "doctor@test.local", "doctor"));

        mockMvc.perform(post("/api/prescriptions")
                        .header("Authorization", doctor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson(90L, 11L, 22L)))
                .andExpect(status().isConflict());
    }

    @Test
    void pendingAndCancelledAppointmentsAreRejected() throws Exception {
        when(appointmentClient.getAppointment(91L)).thenReturn(appointment(91L, 11L, 22L, "PENDING"));
        when(appointmentClient.getAppointment(92L)).thenReturn(appointment(92L, 11L, 22L, "CANCELLED"));
        String doctor = bearer(TestJwtFactory.token(22L, "doctor@test.local", "doctor"));

        mockMvc.perform(post("/api/prescriptions")
                        .header("Authorization", doctor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson(91L, 11L, 22L)))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/prescriptions")
                        .header("Authorization", doctor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson(92L, 11L, 22L)))
                .andExpect(status().isConflict());
    }

    @Test
    void appointmentOutageFailsSafe() throws Exception {
        when(appointmentClient.getAppointment(anyLong())).thenThrow(new RuntimeException("connection refused"));
        String doctor = bearer(TestJwtFactory.token(22L, "doctor@test.local", "doctor"));

        mockMvc.perform(post("/api/prescriptions")
                        .header("Authorization", doctor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson(90L, 11L, 22L)))
                .andExpect(status().isBadGateway());
    }

    @Test
    void incompleteAppointmentResponseFailsSafe() throws Exception {
        AppointmentSnapshot incomplete = new AppointmentSnapshot();
        incomplete.setId(90L);
        when(appointmentClient.getAppointment(90L)).thenReturn(incomplete);
        String doctor = bearer(TestJwtFactory.token(22L, "doctor@test.local", "doctor"));

        mockMvc.perform(post("/api/prescriptions")
                        .header("Authorization", doctor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson(90L, 11L, 22L)))
                .andExpect(status().isBadGateway());
    }

    @Test
    void appointmentForbiddenIsMapped() throws Exception {
        when(appointmentClient.getAppointment(93L)).thenThrow(feignStatus(403));
        String doctor = bearer(TestJwtFactory.token(22L, "doctor@test.local", "doctor"));

        mockMvc.perform(post("/api/prescriptions")
                        .header("Authorization", doctor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson(93L, 11L, 22L)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminDoesNotReceiveUnrestrictedAccess() throws Exception {
        long prescriptionId = createPrescription();
        String admin = bearer(TestJwtFactory.token(1L, "admin@test.local", "admin"));

        mockMvc.perform(get("/api/prescriptions/" + prescriptionId).header("Authorization", admin))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/prescriptions/doctor/me").header("Authorization", admin))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/prescriptions/patient/me").header("Authorization", admin))
                .andExpect(status().isForbidden());
    }

    @Test
    void confirmedAppointmentIsEligible() throws Exception {
        when(appointmentClient.getAppointment(94L)).thenReturn(appointment(94L, 11L, 22L, "CONFIRMED"));
        String doctor = bearer(TestJwtFactory.token(22L, "doctor@test.local", "doctor"));

        mockMvc.perform(post("/api/prescriptions")
                        .header("Authorization", doctor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson(94L, 11L, 22L)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.appointmentId").value(94));
    }

    @Test
    void unknownPrescriptionIsNotFound() throws Exception {
        String doctor = bearer(TestJwtFactory.token(22L, "doctor@test.local", "doctor"));

        mockMvc.perform(get("/api/prescriptions/404").header("Authorization", doctor))
                .andExpect(status().isNotFound());
    }

    private long createPrescription() throws Exception {
        String doctor = bearer(TestJwtFactory.token(22L, "doctor@test.local", "doctor"));
        MvcResult result = mockMvc.perform(post("/api/prescriptions")
                        .header("Authorization", doctor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson(90L, 11L, 22L)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.get("id").asLong();
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
                "/api/appointments/93",
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

    private String createJson(long appointmentId, long patientUserId, long doctorUserId) {
        return """
                {
                  "appointmentId": %d,
                  "patientUserId": %d,
                  "doctorUserId": %d,
                  "diagnosis": "Hypertension",
                  "instructions": "Take with food",
                  "items": [{
                    "medicineName": "Amlodipine",
                    "dosage": "5 mg",
                    "route": "oral",
                    "frequency": "once daily",
                    "duration": "14 days",
                    "quantity": 14,
                    "instructions": "Morning"
                  }]
                }
                """.formatted(appointmentId, patientUserId, doctorUserId);
    }
}
