package com.medicurex.profile.controller;

import com.medicurex.profile.dto.AdminDirectoryResponse;
import com.medicurex.profile.dto.AdminUserProfilesResponse;
import com.medicurex.profile.dto.ApprovalRequest;
import com.medicurex.profile.dto.DoctorProfileResponse;
import com.medicurex.profile.service.AdminProfileService;
import com.medicurex.profile.service.DoctorProfileService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profiles/admin")
public class AdminProfileController {

    private final AdminProfileService adminProfileService;
    private final DoctorProfileService doctorProfileService;

    public AdminProfileController(
            AdminProfileService adminProfileService,
            DoctorProfileService doctorProfileService
    ) {
        this.adminProfileService = adminProfileService;
        this.doctorProfileService = doctorProfileService;
    }

    @GetMapping
    public AdminDirectoryResponse listAll() {
        return adminProfileService.listAll();
    }

    @GetMapping("/{userId}")
    public AdminUserProfilesResponse getByUserId(@PathVariable Long userId) {
        return adminProfileService.getByAuthUserId(userId);
    }

    @PatchMapping("/{userId}/approval")
    public DoctorProfileResponse updateApproval(
            @PathVariable Long userId,
            @Valid @RequestBody ApprovalRequest request
    ) {
        return doctorProfileService.updateApproval(userId, request.getStatus());
    }
}
