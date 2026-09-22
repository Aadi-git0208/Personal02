package com.apexcare.profile.controller;

import com.apexcare.profile.dto.DoctorAvailabilityRequest;
import com.apexcare.profile.dto.DoctorAvailabilityResponse;
import com.apexcare.profile.dto.DoctorProfileRequest;
import com.apexcare.profile.dto.DoctorProfileResponse;
import com.apexcare.profile.security.AuthenticatedUser;
import com.apexcare.profile.service.DoctorProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/profiles/doctor")
public class DoctorProfileController {

    private final DoctorProfileService doctorProfileService;

    public DoctorProfileController(DoctorProfileService doctorProfileService) {
        this.doctorProfileService = doctorProfileService;
    }

    @GetMapping("/me")
    public ResponseEntity<DoctorProfileResponse> me(Authentication authentication) {
        AuthenticatedUser user = AuthenticatedUser.from(authentication);
        return ResponseEntity.ok(doctorProfileService.getMyProfile(user.getUserId()));
    }

    @PutMapping("/me")
    public ResponseEntity<DoctorProfileResponse> upsert(
            Authentication authentication,
            @Valid @RequestBody DoctorProfileRequest request
    ) {
        AuthenticatedUser user = AuthenticatedUser.from(authentication);
        return ResponseEntity.ok(doctorProfileService.upsertMyProfile(user.getUserId(), request));
    }

    @GetMapping("/me/availability")
    public ResponseEntity<List<DoctorAvailabilityResponse>> listAvailability(Authentication authentication) {
        AuthenticatedUser user = AuthenticatedUser.from(authentication);
        return ResponseEntity.ok(doctorProfileService.listMyAvailability(user.getUserId()));
    }

    @PostMapping("/me/availability")
    public ResponseEntity<DoctorAvailabilityResponse> createAvailability(
            Authentication authentication,
            @Valid @RequestBody DoctorAvailabilityRequest request
    ) {
        AuthenticatedUser user = AuthenticatedUser.from(authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(doctorProfileService.createAvailability(user.getUserId(), request));
    }

    @PutMapping("/me/availability/{id}")
    public ResponseEntity<DoctorAvailabilityResponse> updateAvailability(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody DoctorAvailabilityRequest request
    ) {
        AuthenticatedUser user = AuthenticatedUser.from(authentication);
        return ResponseEntity.ok(doctorProfileService.updateAvailability(user.getUserId(), id, request));
    }

    @DeleteMapping("/me/availability/{id}")
    public ResponseEntity<Void> deleteAvailability(Authentication authentication, @PathVariable Long id) {
        AuthenticatedUser user = AuthenticatedUser.from(authentication);
        doctorProfileService.deleteAvailability(user.getUserId(), id);
        return ResponseEntity.noContent().build();
    }
}
