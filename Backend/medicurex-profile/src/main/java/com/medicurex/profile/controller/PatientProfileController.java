package com.medicurex.profile.controller;

import com.medicurex.profile.dto.PatientProfileRequest;
import com.medicurex.profile.dto.PatientProfileResponse;
import com.medicurex.profile.security.AuthenticatedUser;
import com.medicurex.profile.security.SecurityUtils;
import com.medicurex.profile.service.PatientProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profiles/patient")
public class PatientProfileController {

    private final PatientProfileService patientProfileService;

    public PatientProfileController(PatientProfileService patientProfileService) {
        this.patientProfileService = patientProfileService;
    }

    @GetMapping("/me")
    public PatientProfileResponse me() {
        AuthenticatedUser user = SecurityUtils.currentUser();
        return patientProfileService.getMine(user.getUserId());
    }

    @PutMapping("/me")
    public ResponseEntity<PatientProfileResponse> upsert(@Valid @RequestBody PatientProfileRequest request) {
        AuthenticatedUser user = SecurityUtils.currentUser();
        PatientProfileService.UpsertResult result = patientProfileService.upsertMine(user.getUserId(), request);
        HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(result.profile());
    }
}
