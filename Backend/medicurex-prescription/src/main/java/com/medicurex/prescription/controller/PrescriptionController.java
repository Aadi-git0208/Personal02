package com.medicurex.prescription.controller;

import com.medicurex.prescription.dto.CancelPrescriptionRequest;
import com.medicurex.prescription.dto.CreatePrescriptionRequest;
import com.medicurex.prescription.dto.PrescriptionResponse;
import com.medicurex.prescription.security.SecurityUtils;
import com.medicurex.prescription.service.PrescriptionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @PostMapping
    public ResponseEntity<PrescriptionResponse> create(@Valid @RequestBody CreatePrescriptionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(prescriptionService.create(SecurityUtils.currentUser(), request));
    }

    @GetMapping("/doctor/me")
    public Page<PrescriptionResponse> mineAsDoctor(@PageableDefault(size = 20) Pageable pageable) {
        return prescriptionService.listMineAsDoctor(SecurityUtils.currentUser(), pageable);
    }

    @GetMapping("/patient/me")
    public Page<PrescriptionResponse> mineAsPatient(@PageableDefault(size = 20) Pageable pageable) {
        return prescriptionService.listMineAsPatient(SecurityUtils.currentUser(), pageable);
    }

    @GetMapping("/patient/me/{prescriptionId}")
    public PrescriptionResponse getMineAsPatient(@PathVariable Long prescriptionId) {
        return prescriptionService.getForPatient(SecurityUtils.currentUser(), prescriptionId);
    }

    @GetMapping("/{prescriptionId}")
    public PrescriptionResponse get(@PathVariable Long prescriptionId) {
        return prescriptionService.get(SecurityUtils.currentUser(), prescriptionId);
    }

    @PatchMapping("/{prescriptionId}/cancel")
    public PrescriptionResponse cancel(
            @PathVariable Long prescriptionId,
            @RequestBody(required = false) @Valid CancelPrescriptionRequest request
    ) {
        return prescriptionService.cancel(SecurityUtils.currentUser(), prescriptionId, request);
    }
}
