package com.apexcare.auth.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class TestRoleController {

    @GetMapping("/api/patient/test")
    @PreAuthorize("hasRole('PATIENT')")
    public Map<String, String> patientTest() {
        return Map.of("message", "Patient access granted");
    }

    @GetMapping("/api/doctor/test")
    @PreAuthorize("hasRole('DOCTOR')")
    public Map<String, String> doctorTest() {
        return Map.of("message", "Doctor access granted");
    }

    @GetMapping("/api/admin/test")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, String> adminTest() {
        return Map.of("message", "Admin access granted");
    }
}
