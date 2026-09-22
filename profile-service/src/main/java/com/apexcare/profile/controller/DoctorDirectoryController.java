package com.apexcare.profile.controller;

import com.apexcare.profile.dto.DoctorProfileResponse;
import com.apexcare.profile.service.DoctorProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/profiles/doctors")
public class DoctorDirectoryController {

    private final DoctorProfileService doctorProfileService;

    public DoctorDirectoryController(DoctorProfileService doctorProfileService) {
        this.doctorProfileService = doctorProfileService;
    }

    @GetMapping
    public ResponseEntity<List<DoctorProfileResponse>> list(
            @RequestParam(required = false) String specialization
    ) {
        return ResponseEntity.ok(doctorProfileService.listDoctors(specialization));
    }

    @GetMapping("/{doctorId}")
    public ResponseEntity<DoctorProfileResponse> get(@PathVariable Long doctorId) {
        return ResponseEntity.ok(doctorProfileService.getDoctor(doctorId));
    }
}
