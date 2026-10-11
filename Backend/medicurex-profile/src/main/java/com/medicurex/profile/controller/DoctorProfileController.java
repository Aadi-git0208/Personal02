package com.medicurex.profile.controller;

import com.medicurex.profile.dto.DoctorProfileRequest;
import com.medicurex.profile.dto.DoctorProfileResponse;
import com.medicurex.profile.dto.PublicDoctorDirectoryResponse;
import com.medicurex.profile.security.AuthenticatedUser;
import com.medicurex.profile.security.SecurityUtils;
import com.medicurex.profile.service.DoctorProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    public DoctorProfileResponse me() {
        AuthenticatedUser user = SecurityUtils.currentUser();
        return doctorProfileService.getMine(user.getUserId());
    }

    @PutMapping("/me")
    public ResponseEntity<DoctorProfileResponse> upsert(@Valid @RequestBody DoctorProfileRequest request) {
        AuthenticatedUser user = SecurityUtils.currentUser();
        DoctorProfileService.UpsertResult result = doctorProfileService.upsertMine(user.getUserId(), request);
        HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(result.profile());
    }

    @GetMapping("/directory")
    public List<PublicDoctorDirectoryResponse> directory() {
        return doctorProfileService.publicDirectory();
    }

    @GetMapping("/directory/{doctorId}")
    public PublicDoctorDirectoryResponse directoryEntry(@PathVariable Long doctorId) {
        return doctorProfileService.publicById(doctorId);
    }
}
