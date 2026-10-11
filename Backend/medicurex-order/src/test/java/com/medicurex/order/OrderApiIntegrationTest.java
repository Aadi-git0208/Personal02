package com.medicurex.order;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicurex.order.client.MedicineSnapshot;
import com.medicurex.order.client.PharmacyCatalogClient;
import com.medicurex.order.client.PharmacyStockClient;
import com.medicurex.order.client.StockAdjustRequest;
import com.medicurex.order.repository.OrderRepository;
import com.medicurex.order.support.TestJwtFactory;
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

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

    @MockitoBean
    private PharmacyCatalogClient pharmacyCatalogClient;

    @MockitoBean
    private PharmacyStockClient pharmacyStockClient;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        when(pharmacyCatalogClient.getMedicine(1L)).thenReturn(medicine(1L, "Paracetamol", "10.00", "ACTIVE", false));
        when(pharmacyCatalogClient.getMedicine(2L)).thenReturn(medicine(2L, "Cetirizine", "8.50", "ACTIVE", false));
        when(pharmacyStockClient.adjustStock(anyLong(), any(StockAdjustRequest.class)))
                .thenAnswer(invocation -> pharmacyCatalogClient.getMedicine(invocation.getArgument(0)));
        when(pharmacyStockClient.releaseStock(anyLong(), any())).thenReturn(medicine(1L, "Paracetamol", "10.00", "ACTIVE", false));
    }

    @Test
    void missingInvalidAndExpiredJwtsAreRejected() throws Exception {
        mockMvc.perform(get("/api/orders/my")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/orders/my").header("Authorization", "Bearer not-a-token"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/orders/my")
                        .header("Authorization", bearer(TestJwtFactory.expiredToken(11L, "patient@test.local", "patient"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void patientOrderUsesCatalogPriceNotClientPrice() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .header("Authorization", patient())
                        .header("X-User-Id", "999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(1L, 2, "1.00", "checkout-1")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.patientUserId").value(11))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.items[0].unitPrice").value(10.00))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].lineTotal").value(20.00))
                .andExpect(jsonPath("$.subtotal").value(20.00))
                .andExpect(jsonPath("$.deliveryFee").value(40.00))
                .andExpect(jsonPath("$.totalAmount").value(60.00));
    }

    @Test
    void nonpositiveQuantityIsRejected() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .header("Authorization", patient())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(1L, 0, "10.00", "qty-0")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void inactiveMedicineIsRejected() throws Exception {
        when(pharmacyCatalogClient.getMedicine(3L)).thenReturn(medicine(3L, "Hidden", "5.00", "INACTIVE", false));

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", patient())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(3L, 1, "5.00", "inactive")))
                .andExpect(status().isConflict());
        verify(pharmacyStockClient, never()).adjustStock(anyLong(), any());
    }

    @Test
    void prescriptionRequiredMedicineIsRejected() throws Exception {
        when(pharmacyCatalogClient.getMedicine(4L)).thenReturn(medicine(4L, "Amlodipine", "12.50", "ACTIVE", true));

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", patient())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(4L, 1, "12.50", "rx-required")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Prescription-required")));
        verify(pharmacyStockClient, never()).adjustStock(anyLong(), any());
    }

    @Test
    void catalogOutageFailsSafe() throws Exception {
        when(pharmacyCatalogClient.getMedicine(anyLong())).thenThrow(new RuntimeException("connection refused"));

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", patient())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(1L, 1, "10.00", "outage")))
                .andExpect(status().isBadGateway());
    }

    @Test
    void reservationFailureCompensatesEarlierItems() throws Exception {
        when(pharmacyStockClient.adjustStock(eq(2L), any())).thenThrow(feignStatus(409));

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", patient())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "idempotencyKey": "two-items",
                                  "recipientName": "Pat Patient",
                                  "phone": "5551234567",
                                  "address": "1 Main St",
                                  "items": [
                                    {"medicineId": 1, "quantity": 1},
                                    {"medicineId": 2, "quantity": 1}
                                  ]
                                }
                                """))
                .andExpect(status().isConflict());

        verify(pharmacyStockClient).adjustStock(eq(1L), any());
        verify(pharmacyStockClient).releaseStock(eq(1L), any());
    }

    @Test
    void duplicateIdempotencyKeyReturnsSameOrder() throws Exception {
        long orderId = createOrder("dup-key");

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", patient())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(1L, 2, "99.00", "dup-key")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(orderId));

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", patient())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "idempotencyKey": "dup-key",
                                  "recipientName": "Someone Else",
                                  "phone": "5551234567",
                                  "address": "2 Other St",
                                  "items": [{"medicineId": 1, "quantity": 2, "unitPrice": 99.00}]
                                }
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void patientCanReadOwnOrderAndCancel() throws Exception {
        long orderId = createOrder("cancel-me");
        String other = bearer(TestJwtFactory.token(12L, "other@test.local", "patient"));

        mockMvc.perform(get("/api/orders/" + orderId).header("Authorization", other))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/orders/" + orderId).header("Authorization", patient()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId));

        mockMvc.perform(patch("/api/orders/" + orderId + "/cancel")
                        .header("Authorization", patient())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Changed mind\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(pharmacyStockClient, times(1)).releaseStock(eq(1L), any());

        mockMvc.perform(patch("/api/orders/" + orderId + "/cancel").header("Authorization", patient()))
                .andExpect(status().isConflict());
    }

    @Test
    void patientCannotMarkShippedAndAdminCanFulfill() throws Exception {
        long orderId = createOrder("fulfill");

        mockMvc.perform(patch("/api/orders/" + orderId + "/status")
                        .header("Authorization", patient())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"SHIPPED\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/orders/" + orderId + "/status")
                        .header("Authorization", admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DELIVERED\"}"))
                .andExpect(status().isConflict());

        mockMvc.perform(patch("/api/orders/" + orderId + "/status")
                        .header("Authorization", admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PROCESSING\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PROCESSING"));

        mockMvc.perform(get("/api/orders/" + orderId + "/admin").header("Authorization", admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId));

        mockMvc.perform(get("/api/orders").header("Authorization", admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void doctorCannotCreateOrder() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .header("Authorization", bearer(TestJwtFactory.token(22L, "doctor@test.local", "doctor")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(1L, 1, "10.00", "doc")))
                .andExpect(status().isForbidden());
    }

    private long createOrder(String idempotencyKey) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/orders")
                        .header("Authorization", patient())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(1L, 2, "99.00", idempotencyKey)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.get("id").asLong();
    }

    private MedicineSnapshot medicine(long id, String name, String price, String status, boolean rxRequired) {
        MedicineSnapshot snapshot = new MedicineSnapshot();
        snapshot.setId(id);
        snapshot.setName(name);
        snapshot.setStrength("500 mg");
        snapshot.setDosageForm("TABLET");
        snapshot.setUnitPrice(new BigDecimal(price));
        snapshot.setStockQuantity(20);
        snapshot.setStatus(status);
        snapshot.setPrescriptionRequired(rxRequired);
        return snapshot;
    }

    private FeignException feignStatus(int status) {
        Request request = Request.create(
                Request.HttpMethod.POST,
                "/internal/medicines/2/stock/adjust",
                Map.of(),
                null,
                StandardCharsets.UTF_8,
                new RequestTemplate()
        );
        Response response = Response.builder()
                .status(status)
                .reason("Conflict")
                .request(request)
                .headers(Map.of())
                .build();
        return FeignException.errorStatus("adjustStock", response);
    }

    private String patient() {
        return bearer(TestJwtFactory.token(11L, "patient@test.local", "patient"));
    }

    private String admin() {
        return bearer(TestJwtFactory.token(1L, "admin@test.local", "admin"));
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String orderJson(long medicineId, int quantity, String clientPrice, String idempotencyKey) {
        return """
                {
                  "idempotencyKey": "%s",
                  "recipientName": "Pat Patient",
                  "phone": "5551234567",
                  "address": "1 Main St",
                  "patientUserId": 999,
                  "items": [{
                    "medicineId": %d,
                    "quantity": %d,
                    "unitPrice": %s
                  }]
                }
                """.formatted(idempotencyKey, medicineId, quantity, clientPrice);
    }
}
