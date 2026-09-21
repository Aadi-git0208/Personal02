package com.apexcare.auth;

import com.apexcare.auth.model.Role;
import com.apexcare.auth.model.User;
import com.apexcare.auth.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    @Test
    void registerPatientSuccessfully() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("Rahul", "rahul@gmail.com", "rahul123", "patient")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Rahul"))
                .andExpect(jsonPath("$.email").value("rahul@gmail.com"))
                .andExpect(jsonPath("$.role").value("patient"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void registerDoctorSuccessfully() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("Dr Sharma", "doctor@gmail.com", "doctor123", "doctor")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("doctor"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void rejectAdminRegistration() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("Admin", "admin@gmail.com", "admin123", "admin")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void rejectDuplicateEmail() throws Exception {
        registerUser("Rahul", "rahul@gmail.com", "rahul123", "patient");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("Rahul Two", "rahul@gmail.com", "rahul123", "patient")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email is already registered"));
    }

    @Test
    void passwordIsStoredHashed() throws Exception {
        registerUser("Rahul", "rahul@gmail.com", "rahul123", "patient");

        User saved = userRepository.findByEmail("rahul@gmail.com").orElseThrow();
        assertThat(saved.getPassword()).isNotEqualTo("rahul123");
        assertThat(saved.getPassword()).startsWith("$2");
        assertThat(passwordEncoder.matches("rahul123", saved.getPassword())).isTrue();
    }

    @Test
    void loginPatientSuccessfully() throws Exception {
        registerUser("Rahul", "rahul@gmail.com", "rahul123", "patient");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("rahul@gmail.com", "rahul123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value("rahul@gmail.com"))
                .andExpect(jsonPath("$.user.role").value("patient"))
                .andExpect(jsonPath("$.user.password").doesNotExist());
    }

    @Test
    void loginDoctorSuccessfully() throws Exception {
        registerUser("Dr Sharma", "doctor@gmail.com", "doctor123", "doctor");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("doctor@gmail.com", "doctor123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.role").value("doctor"));
    }

    @Test
    void rejectInvalidPassword() throws Exception {
        registerUser("Rahul", "rahul@gmail.com", "rahul123", "patient");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("rahul@gmail.com", "wrong-password")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void rejectUnknownEmail() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("missing@gmail.com", "rahul123")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void loginReturnsJwt() throws Exception {
        registerUser("Rahul", "rahul@gmail.com", "rahul123", "patient");
        String token = loginAndGetToken("rahul@gmail.com", "rahul123");
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void meWorksWithValidJwt() throws Exception {
        registerUser("Rahul", "rahul@gmail.com", "rahul123", "patient");
        String token = loginAndGetToken("rahul@gmail.com", "rahul123");

        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("rahul@gmail.com"))
                .andExpect(jsonPath("$.role").value("patient"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void meRejectsMissingJwt() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meRejectsInvalidJwt() throws Exception {
        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer not-a-valid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void patientCanAccessPatientEndpoint() throws Exception {
        String token = registerAndLogin("Rahul", "rahul@gmail.com", "rahul123", "patient");

        mockMvc.perform(get("/api/patient/test").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Patient access granted"));
    }

    @Test
    void patientCannotAccessDoctorEndpoint() throws Exception {
        String token = registerAndLogin("Rahul", "rahul@gmail.com", "rahul123", "patient");

        mockMvc.perform(get("/api/doctor/test").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void patientCannotAccessAdminEndpoint() throws Exception {
        String token = registerAndLogin("Rahul", "rahul@gmail.com", "rahul123", "patient");

        mockMvc.perform(get("/api/admin/test").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void doctorCanAccessDoctorEndpoint() throws Exception {
        String token = registerAndLogin("Dr Sharma", "doctor@gmail.com", "doctor123", "doctor");

        mockMvc.perform(get("/api/doctor/test").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Doctor access granted"));
    }

    @Test
    void doctorCannotAccessAdminEndpoint() throws Exception {
        String token = registerAndLogin("Dr Sharma", "doctor@gmail.com", "doctor123", "doctor");

        mockMvc.perform(get("/api/admin/test").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanAccessAdminEndpoint() throws Exception {
        String token = createAdminAndLogin();

        mockMvc.perform(get("/api/admin/test").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Admin access granted"));
    }

    private String createAdminAndLogin() throws Exception {
        User admin = new User();
        admin.setName("Administrator");
        admin.setEmail("admin@medicurex.local");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);
        return loginAndGetToken("admin@medicurex.local", "admin123");
    }

    private String registerAndLogin(String name, String email, String password, String role) throws Exception {
        registerUser(name, email, password, role);
        return loginAndGetToken(email, password);
    }

    private void registerUser(String name, String email, String password, String role) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson(name, email, password, role)))
                .andExpect(status().isCreated());
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(email, password)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.get("token").asText();
    }

    private String registerJson(String name, String email, String password, String role) {
        return """
                {
                  "name": "%s",
                  "email": "%s",
                  "password": "%s",
                  "role": "%s"
                }
                """.formatted(name, email, password, role);
    }

    private String loginJson(String email, String password) {
        return """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(email, password);
    }
}
