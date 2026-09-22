package com.apexcare.order;

import com.apexcare.order.client.MedicineSnapshot;
import com.apexcare.order.client.PharmacyClient;
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

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PharmacyClient pharmacyClient;

    @BeforeEach
    void stubPharmacy() {
        when(pharmacyClient.getMedicine(eq(2L), any())).thenReturn(medicine(2L, "Paracetamol", "50.00", 20));
        when(pharmacyClient.adjustStock(eq(2L), eq(-2), any())).thenReturn(medicine(2L, "Paracetamol", "50.00", 18));
        when(pharmacyClient.adjustStock(eq(2L), eq(2), any())).thenReturn(medicine(2L, "Paracetamol", "50.00", 20));
    }

    @Test
    void unauthenticatedCartIsRejected() throws Exception {
        mockMvc.perform(get("/api/orders/cart"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void doctorCannotUseCart() throws Exception {
        String doctorToken = TestJwtFactory.token(21L, "doctor@gmail.com", "doctor");
        mockMvc.perform(get("/api/orders/cart")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void patientCanCreateCartAddAndUpdateItem() throws Exception {
        String patientToken = TestJwtFactory.token(11L, "rahul@gmail.com", "patient");

        mockMvc.perform(get("/api/orders/cart")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value(11))
                .andExpect(jsonPath("$.items").isEmpty());

        mockMvc.perform(post("/api/orders/cart/items")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"medicineId\":2,\"quantity\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.items[0].quantity").value(1))
                .andExpect(jsonPath("$.totalAmount").value(50.00));

        mockMvc.perform(put("/api/orders/cart/items/2")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.totalAmount").value(100.00));
    }

    @Test
    void insufficientStockIsRejected() throws Exception {
        String patientToken = TestJwtFactory.token(11L, "rahul@gmail.com", "patient");
        when(pharmacyClient.getMedicine(eq(2L), any())).thenReturn(medicine(2L, "Paracetamol", "50.00", 1));

        mockMvc.perform(post("/api/orders/cart/items")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"medicineId\":2,\"quantity\":5}"))
                .andExpect(status().isConflict());
    }

    @Test
    void checkoutSnapshotsPharmacyPriceAndClearsCart() throws Exception {
        String patientToken = TestJwtFactory.token(11L, "rahul@gmail.com", "patient");
        addTwo(patientToken);

        mockMvc.perform(post("/api/orders/checkout")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.patientId").value(11))
                .andExpect(jsonPath("$.status").value("PLACED"))
                .andExpect(jsonPath("$.totalAmount").value(100.00))
                .andExpect(jsonPath("$.items[0].priceSnapshot").value(50.00))
                .andExpect(jsonPath("$.items[0].subtotal").value(100.00));

        mockMvc.perform(get("/api/orders/cart")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty());

        mockMvc.perform(get("/api/orders/my-orders")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].totalAmount").value(100.00));
    }

    @Test
    void otherPatientCannotReadOrder() throws Exception {
        String owner = TestJwtFactory.token(11L, "rahul@gmail.com", "patient");
        String other = TestJwtFactory.token(12L, "other@gmail.com", "patient");
        addTwo(owner);
        long orderId = checkout(owner);

        mockMvc.perform(get("/api/orders/" + orderId)
                        .header("Authorization", "Bearer " + other))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanListAndAdvanceStatus() throws Exception {
        String patientToken = TestJwtFactory.token(11L, "rahul@gmail.com", "patient");
        String adminToken = TestJwtFactory.token(1L, "admin@medicurex.local", "admin");
        addTwo(patientToken);
        long orderId = checkout(patientToken);

        mockMvc.perform(get("/api/orders")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(orderId));

        mockMvc.perform(patch("/api/orders/" + orderId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CONFIRMED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void adminCancelRestoresStock() throws Exception {
        String patientToken = TestJwtFactory.token(11L, "rahul@gmail.com", "patient");
        String adminToken = TestJwtFactory.token(1L, "admin@medicurex.local", "admin");
        addTwo(patientToken);
        long orderId = checkout(patientToken);

        mockMvc.perform(patch("/api/orders/" + orderId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CANCELLED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    private void addTwo(String patientToken) throws Exception {
        mockMvc.perform(post("/api/orders/cart/items")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"medicineId\":2,\"quantity\":2}"))
                .andExpect(status().isCreated());
    }

    private long checkout(String patientToken) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/orders/checkout")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asLong();
    }

    private static MedicineSnapshot medicine(Long id, String name, String price, int stock) {
        MedicineSnapshot snapshot = new MedicineSnapshot();
        snapshot.setId(id);
        snapshot.setName(name);
        snapshot.setPrice(new BigDecimal(price));
        snapshot.setStockQuantity(stock);
        snapshot.setActive(true);
        return snapshot;
    }
}
