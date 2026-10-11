package com.medicurex.pharmacy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicurex.pharmacy.repository.MedicineRepository;
import com.medicurex.pharmacy.repository.StockAdjustmentRepository;
import com.medicurex.pharmacy.support.TestJwtFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PharmacyApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private StockAdjustmentRepository stockAdjustmentRepository;

    @BeforeEach
    void setUp() {
        stockAdjustmentRepository.deleteAll();
        medicineRepository.deleteAll();
    }

    @Test
    void publicCatalogCanBeListedAndPaginated() throws Exception {
        createMedicine("Amlodipine", "5 mg", "Cardiovascular", 20);
        createMedicine("Paracetamol", "500 mg", "Analgesic", 10);
        createMedicine("Cetirizine", "10 mg", "Allergy", 8);

        mockMvc.perform(get("/api/medicines?page=0&size=2&sort=name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].name").value("Amlodipine"))
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    void searchFindsByNameAndGeneric() throws Exception {
        createMedicine("Amlodipine", "5 mg", "Cardiovascular", 20);

        mockMvc.perform(get("/api/medicines/search").param("q", "amlo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("Amlodipine"));

        mockMvc.perform(get("/api/medicines").param("q", "amlodipine besylate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void adminCanCreateMedicine() throws Exception {
        mockMvc.perform(post("/api/medicines")
                        .header("Authorization", admin())
                        .header("X-Role", "PATIENT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson("Amlodipine", "5 mg", "Cardiovascular", 12.50, 20)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Amlodipine"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.unitPrice").value(12.50))
                .andExpect(jsonPath("$.stockQuantity").value(20))
                .andExpect(jsonPath("$.prescriptionRequired").value(true));
    }

    @Test
    void missingInvalidAndExpiredJwtsAreRejectedOnWrites() throws Exception {
        mockMvc.perform(post("/api/medicines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson("Amlodipine", "5 mg", "Cardiovascular", 10, 5)))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/medicines")
                        .header("Authorization", "Bearer not-a-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson("Amlodipine", "5 mg", "Cardiovascular", 10, 5)))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/medicines")
                        .header("Authorization", bearer(TestJwtFactory.expiredToken(1L, "admin@test.local", "admin")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson("Amlodipine", "5 mg", "Cardiovascular", 10, 5)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void patientCannotWriteCatalog() throws Exception {
        String patient = bearer(TestJwtFactory.token(11L, "patient@test.local", "patient"));

        mockMvc.perform(post("/api/medicines")
                        .header("Authorization", patient)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson("Amlodipine", "5 mg", "Cardiovascular", 10, 5)))
                .andExpect(status().isForbidden());
    }

    @Test
    void doctorCannotChangePriceOrStock() throws Exception {
        long id = createMedicine("Amlodipine", "5 mg", "Cardiovascular", 20);
        String doctor = bearer(TestJwtFactory.token(22L, "doctor@test.local", "doctor"));

        mockMvc.perform(put("/api/medicines/" + id)
                        .header("Authorization", doctor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson("Amlodipine", "5 mg", "Cardiovascular", 1.00)))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/medicines/" + id + "/stock")
                        .header("Authorization", doctor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":999}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void negativePriceAndStockAreRejected() throws Exception {
        mockMvc.perform(post("/api/medicines")
                        .header("Authorization", admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson("Amlodipine", "5 mg", "Cardiovascular", -1, 5)))
                .andExpect(status().isBadRequest());

        long id = createMedicine("Paracetamol", "500 mg", "Analgesic", 10);
        mockMvc.perform(patch("/api/medicines/" + id + "/stock")
                        .header("Authorization", admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":-3}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void zeroStockIsOutOfStockAndRestockReactivates() throws Exception {
        mockMvc.perform(post("/api/medicines")
                        .header("Authorization", admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson("Cetirizine", "10 mg", "Allergy", 8.00, 0)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("OUT_OF_STOCK"));

        long id = createMedicine("Amlodipine", "5 mg", "Cardiovascular", 4);
        mockMvc.perform(patch("/api/medicines/" + id + "/stock")
                        .header("Authorization", admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OUT_OF_STOCK"));

        mockMvc.perform(patch("/api/medicines/" + id + "/stock")
                        .header("Authorization", admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":6}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.stockQuantity").value(6));
    }

    @Test
    void deactivationHidesMedicineFromPublicCatalog() throws Exception {
        long id = createMedicine("Amlodipine", "5 mg", "Cardiovascular", 20);

        mockMvc.perform(patch("/api/medicines/" + id + "/status")
                        .header("Authorization", admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"INACTIVE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));

        mockMvc.perform(get("/api/medicines"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));

        mockMvc.perform(get("/api/medicines/" + id))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/medicines/" + id).header("Authorization", admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    void duplicateIdentityIsRejected() throws Exception {
        createMedicine("Amlodipine", "5 mg", "Cardiovascular", 20);

        mockMvc.perform(post("/api/medicines")
                        .header("Authorization", admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson(" amlodipine ", "5 mg", "Cardiovascular", 9.00, 3)))
                .andExpect(status().isConflict());
    }

    @Test
    void unknownMedicineIsNotFound() throws Exception {
        mockMvc.perform(get("/api/medicines/404"))
                .andExpect(status().isNotFound());
    }

    @Test
    void internalStockAdjustRequiresServiceToken() throws Exception {
        long id = createMedicine("Amlodipine", "5 mg", "Cardiovascular", 5);

        mockMvc.perform(post("/internal/medicines/" + id + "/stock/adjust")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"delta\":-1,\"operationKey\":\"reserve-1\"}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/internal/medicines/" + id + "/stock/adjust")
                        .header("X-Internal-Token", "wrong")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"delta\":-1,\"operationKey\":\"reserve-1\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void internalStockAdjustIsIdempotentAndRejectsOverSell() throws Exception {
        long id = createMedicine("Amlodipine", "5 mg", "Cardiovascular", 2);

        mockMvc.perform(post("/internal/medicines/" + id + "/stock/adjust")
                        .header("X-Internal-Token", "test-internal-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"delta\":-2,\"operationKey\":\"reserve-amlodipine\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(0))
                .andExpect(jsonPath("$.status").value("OUT_OF_STOCK"));

        mockMvc.perform(post("/internal/medicines/" + id + "/stock/adjust")
                        .header("X-Internal-Token", "test-internal-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"delta\":-2,\"operationKey\":\"reserve-amlodipine\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(0));

        mockMvc.perform(post("/internal/medicines/" + id + "/stock/adjust")
                        .header("X-Internal-Token", "test-internal-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"delta\":-1,\"operationKey\":\"reserve-amlodipine-extra\"}"))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/internal/medicines/" + id + "/stock/release")
                        .header("X-Internal-Token", "test-internal-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"operationKey\":\"reserve-amlodipine\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(2))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void categoriesAreListedForBrowsableMedicines() throws Exception {
        createMedicine("Amlodipine", "5 mg", "Cardiovascular", 20);
        createMedicine("Paracetamol", "500 mg", "Analgesic", 10);

        mockMvc.perform(get("/api/medicines/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories", hasSize(2)));
    }

    private long createMedicine(String name, String strength, String category, int stock) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/medicines")
                        .header("Authorization", admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson(name, strength, category, 10.00, stock)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.get("id").asLong();
    }

    private String admin() {
        return bearer(TestJwtFactory.token(1L, "admin@test.local", "admin"));
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String createJson(String name, String strength, String category, double price, int stock) {
        return """
                {
                  "name": "%s",
                  "genericName": "Amlodipine besylate",
                  "manufacturer": "MediCurex Labs",
                  "description": "Catalog sample. Not medical advice.",
                  "dosageForm": "TABLET",
                  "strength": "%s",
                  "category": "%s",
                  "unitPrice": %s,
                  "stockQuantity": %d,
                  "prescriptionRequired": true
                }
                """.formatted(name, strength, category, price, stock);
    }

    private String updateJson(String name, String strength, String category, double price) {
        return """
                {
                  "name": "%s",
                  "genericName": "Amlodipine besylate",
                  "manufacturer": "MediCurex Labs",
                  "description": "Updated catalog entry",
                  "dosageForm": "TABLET",
                  "strength": "%s",
                  "category": "%s",
                  "unitPrice": %s,
                  "prescriptionRequired": true
                }
                """.formatted(name, strength, category, price);
    }
}
