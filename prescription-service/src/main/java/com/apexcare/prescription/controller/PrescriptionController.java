package com.apexcare.prescription.controller;

import com.apexcare.prescription.dto.CreatePrescriptionRequest;
import com.apexcare.prescription.dto.PrescriptionResponse;
import com.apexcare.prescription.security.AuthenticatedUser;
import com.apexcare.prescription.service.PrescriptionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @PostMapping
    public ResponseEntity<PrescriptionResponse> create(
            Authentication authentication,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @Valid @RequestBody CreatePrescriptionRequest request
    ) {
        AuthenticatedUser user = AuthenticatedUser.from(authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(prescriptionService.create(user, authorization, request));
    }

    @GetMapping("/doctor/me")
    public ResponseEntity<List<PrescriptionResponse>> doctorMe(Authentication authentication) {
        return ResponseEntity.ok(prescriptionService.listForDoctor(AuthenticatedUser.from(authentication)));
    }

    @GetMapping("/patient/me")
    public ResponseEntity<List<PrescriptionResponse>> patientMe(Authentication authentication) {
        return ResponseEntity.ok(prescriptionService.listForPatient(AuthenticatedUser.from(authentication)));
    }

    @GetMapping
    public ResponseEntity<List<PrescriptionResponse>> listAll() {
        return ResponseEntity.ok(prescriptionService.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PrescriptionResponse> get(Authentication authentication, @PathVariable Long id) {
        return ResponseEntity.ok(prescriptionService.get(AuthenticatedUser.from(authentication), id));
    }
}
