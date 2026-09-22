package com.apexcare.pharmacy;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PharmacyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void catalogIsPublic() throws Exception {
        mockMvc.perform(get("/api/medicines"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void patientCannotCreateMedicine() throws Exception {
        String patientToken = TestJwtFactory.token(11L, "rahul@gmail.com", "patient");

        mockMvc.perform(post("/api/medicines")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody("Paracetamol", "50.00", 20)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanCreateSearchAndLookupMedicine() throws Exception {
        String adminToken = TestJwtFactory.token(1L, "admin@medicurex.local", "admin");
        long id = create(adminToken, "Paracetamol", "50.00", 20);

        mockMvc.perform(get("/api/medicines/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Paracetamol"))
                .andExpect(jsonPath("$.price").value(50.00))
                .andExpect(jsonPath("$.stockQuantity").value(20));

        mockMvc.perform(get("/api/medicines/search").param("query", "para"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id));
    }

    @Test
    void negativePriceIsRejected() throws Exception {
        String adminToken = TestJwtFactory.token(1L, "admin@medicurex.local", "admin");

        mockMvc.perform(post("/api/medicines")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody("Ibuprofen", "-1.00", 10)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void stockAdjustmentCannotGoNegative() throws Exception {
        String adminToken = TestJwtFactory.token(1L, "admin@medicurex.local", "admin");
        long id = create(adminToken, "Amoxicillin", "80.00", 2);

        mockMvc.perform(patch("/api/medicines/" + id + "/stock")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"adjustment\":-5}"))
                .andExpect(status().isConflict());
    }

    @Test
    void adminCanSetStockAndSoftDelete() throws Exception {
        String adminToken = TestJwtFactory.token(1L, "admin@medicurex.local", "admin");
        long id = create(adminToken, "Cetirizine", "30.00", 8);

        mockMvc.perform(patch("/api/medicines/" + id + "/stock")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stockQuantity\":15}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(15));

        mockMvc.perform(delete("/api/medicines/" + id)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/medicines/" + id))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/medicines"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id==" + id + ")]").doesNotExist());
    }

    @Test
    void doctorCannotManageInventory() throws Exception {
        String doctorToken = TestJwtFactory.token(21L, "doctor@gmail.com", "doctor");

        mockMvc.perform(post("/api/medicines")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody("Azithromycin", "120.00", 4)))
                .andExpect(status().isForbidden());
    }

    @Test
    void patientCanAdjustStockButCannotSetAbsoluteQuantity() throws Exception {
        String adminToken = TestJwtFactory.token(1L, "admin@medicurex.local", "admin");
        String patientToken = TestJwtFactory.token(11L, "rahul@gmail.com", "patient");
        long id = create(adminToken, "ORS", "25.00", 10);

        mockMvc.perform(patch("/api/medicines/" + id + "/stock")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"adjustment\":-2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(8));

        mockMvc.perform(patch("/api/medicines/" + id + "/stock")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stockQuantity\":50}"))
                .andExpect(status().isForbidden());
    }

    private long create(String adminToken, String name, String price, int stock) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/medicines")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody(name, price, stock)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asLong();
    }

    private static String createBody(String name, String price, int stock) {
        return """
                {
                  "name": "%s",
                  "description": "Test medicine",
                  "category": "General",
                  "price": %s,
                  "stockQuantity": %d
                }
                """.formatted(name, price, stock);
    }
}
