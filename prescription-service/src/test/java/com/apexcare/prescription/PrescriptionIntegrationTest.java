package com.apexcare.prescription;

import com.apexcare.prescription.client.AppointmentClient;
import com.apexcare.prescription.client.AppointmentSnapshot;
import com.apexcare.prescription.client.MedicineSnapshot;
import com.apexcare.prescription.client.PharmacyClient;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PrescriptionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AppointmentClient appointmentClient;

    @MockitoBean
    private PharmacyClient pharmacyClient;

    @BeforeEach
    void stubDependencies() {
        when(appointmentClient.getAppointment(eq(1L), any())).thenReturn(appointment(1L, "COMPLETED"));
        when(appointmentClient.getAppointment(eq(2L), any())).thenReturn(appointment(2L, "PENDING"));
        MedicineSnapshot medicine = new MedicineSnapshot();
        medicine.setId(9L);
        medicine.setName("Paracetamol");
        when(pharmacyClient.getMedicine(eq(9L), any())).thenReturn(medicine);
    }

    @Test
    void unauthenticatedAccessIsRejected() throws Exception {
        mockMvc.perform(get("/api/prescriptions/patient/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void patientCannotCreatePrescription() throws Exception {
        String patientToken = TestJwtFactory.token(11L, "rahul@gmail.com", "patient");

        mockMvc.perform(post("/api/prescriptions")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody(1L, null, "Amoxicillin")))
                .andExpect(status().isForbidden());
    }

    @Test
    void doctorCreatesPrescriptionUsingAppointmentIdentity() throws Exception {
        String doctorToken = TestJwtFactory.token(21L, "doctor@gmail.com", "doctor");

        mockMvc.perform(post("/api/prescriptions")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody(1L, 9L, "Ignored")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.patientId").value(11))
                .andExpect(jsonPath("$.doctorId").value(21))
                .andExpect(jsonPath("$.appointmentId").value(1))
                .andExpect(jsonPath("$.diagnosis").value("Viral fever"))
                .andExpect(jsonPath("$.items[0].medicineId").value(9))
                .andExpect(jsonPath("$.items[0].medicineName").value("Paracetamol"))
                .andExpect(jsonPath("$.items[0].dosage").value("500mg"));
    }

    @Test
    void pendingAppointmentIsRejected() throws Exception {
        String doctorToken = TestJwtFactory.token(21L, "doctor@gmail.com", "doctor");

        mockMvc.perform(post("/api/prescriptions")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody(2L, null, "Amoxicillin")))
                .andExpect(status().isConflict());
    }

    @Test
    void otherDoctorCannotCreateOrRead() throws Exception {
        String owner = TestJwtFactory.token(21L, "doctor@gmail.com", "doctor");
        String other = TestJwtFactory.token(22L, "otherdoc@gmail.com", "doctor");
        long id = create(owner, 1L);

        mockMvc.perform(post("/api/prescriptions")
                        .header("Authorization", "Bearer " + other)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody(1L, null, "Amoxicillin")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/prescriptions/" + id)
                        .header("Authorization", "Bearer " + other))
                .andExpect(status().isForbidden());
    }

    @Test
    void patientCanReadOwnPrescriptionButNotAnotherPatients() throws Exception {
        String doctorToken = TestJwtFactory.token(21L, "doctor@gmail.com", "doctor");
        String patientToken = TestJwtFactory.token(11L, "rahul@gmail.com", "patient");
        String otherPatient = TestJwtFactory.token(12L, "other@gmail.com", "patient");
        long id = create(doctorToken, 1L);

        mockMvc.perform(get("/api/prescriptions/patient/me")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id));

        mockMvc.perform(get("/api/prescriptions/" + id)
                        .header("Authorization", "Bearer " + otherPatient))
                .andExpect(status().isForbidden());
    }

    @Test
    void duplicatePrescriptionForSameAppointmentIsRejected() throws Exception {
        String doctorToken = TestJwtFactory.token(21L, "doctor@gmail.com", "doctor");
        create(doctorToken, 1L);

        mockMvc.perform(post("/api/prescriptions")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody(1L, null, "Amoxicillin")))
                .andExpect(status().isConflict());
    }

    @Test
    void missingDosageIsRejected() throws Exception {
        String doctorToken = TestJwtFactory.token(21L, "doctor@gmail.com", "doctor");

        mockMvc.perform(post("/api/prescriptions")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "appointmentId": 1,
                                  "diagnosis": "Fever",
                                  "items": [
                                    {
                                      "medicineName": "Amoxicillin",
                                      "frequency": "Twice daily",
                                      "duration": "5 days"
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminCanListAll() throws Exception {
        String doctorToken = TestJwtFactory.token(21L, "doctor@gmail.com", "doctor");
        String adminToken = TestJwtFactory.token(1L, "admin@medicurex.local", "admin");
        create(doctorToken, 1L);

        mockMvc.perform(get("/api/prescriptions")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].patientId").value(11));
    }

    private long create(String doctorToken, long appointmentId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/prescriptions")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody(appointmentId, 9L, "Ignored")))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asLong();
    }

    private static String createBody(long appointmentId, Long medicineId, String medicineName) {
        String medicine = medicineId == null
                ? "\"medicineName\": \"%s\"".formatted(medicineName)
                : "\"medicineId\": %d, \"medicineName\": \"%s\"".formatted(medicineId, medicineName);
        return """
                {
                  "appointmentId": %d,
                  "diagnosis": "Viral fever",
                  "notes": "Rest and fluids",
                  "items": [
                    {
                      %s,
                      "dosage": "500mg",
                      "frequency": "Twice daily",
                      "duration": "5 days",
                      "instructions": "After food"
                    }
                  ]
                }
                """.formatted(appointmentId, medicine);
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
