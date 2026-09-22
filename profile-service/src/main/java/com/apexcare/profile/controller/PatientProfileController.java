package com.apexcare.profile.controller;

import com.apexcare.profile.dto.PatientProfileRequest;
import com.apexcare.profile.dto.PatientProfileResponse;
import com.apexcare.profile.security.AuthenticatedUser;
import com.apexcare.profile.service.PatientProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
    public ResponseEntity<PatientProfileResponse> me(Authentication authentication) {
        AuthenticatedUser user = AuthenticatedUser.from(authentication);
        return ResponseEntity.ok(patientProfileService.getMyProfile(user.getUserId()));
    }

    @PutMapping("/me")
    public ResponseEntity<PatientProfileResponse> upsert(
            Authentication authentication,
            @Valid @RequestBody PatientProfileRequest request
    ) {
        AuthenticatedUser user = AuthenticatedUser.from(authentication);
        return ResponseEntity.ok(patientProfileService.upsertMyProfile(user.getUserId(), request));
    }
}
