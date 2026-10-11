package com.medicurex.appointment.controller;

import com.medicurex.appointment.dto.AppointmentResponse;
import com.medicurex.appointment.dto.CreateAppointmentRequest;
import com.medicurex.appointment.dto.DoctorAvailabilityResponse;
import com.medicurex.appointment.dto.StatusChangeRequest;
import com.medicurex.appointment.security.AuthenticatedUser;
import com.medicurex.appointment.security.SecurityUtils;
import com.medicurex.appointment.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public ResponseEntity<AppointmentResponse> create(@Valid @RequestBody CreateAppointmentRequest request) {
        AuthenticatedUser user = SecurityUtils.currentUser();
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.create(user, request));
    }

    @GetMapping("/patient/me")
    public Page<AppointmentResponse> myPatientAppointments(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return appointmentService.listMineAsPatient(SecurityUtils.currentUser(), pageable);
    }

    @GetMapping("/doctor/me")
    public Page<AppointmentResponse> myDoctorAppointments(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return appointmentService.listMineAsDoctor(SecurityUtils.currentUser(), pageable);
    }

    @GetMapping("/admin")
    public Page<AppointmentResponse> adminList(@PageableDefault(size = 20) Pageable pageable) {
        return appointmentService.listAllForAdmin(pageable);
    }

    @GetMapping("/admin/{appointmentId}")
    public AppointmentResponse adminGet(@PathVariable Long appointmentId) {
        return appointmentService.getById(SecurityUtils.currentUser(), appointmentId);
    }

    @GetMapping("/doctor/{doctorProfileId}/availability")
    public DoctorAvailabilityResponse availability(
            @PathVariable Long doctorProfileId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return appointmentService.availability(doctorProfileId, date);
    }

    @GetMapping("/{appointmentId}")
    public AppointmentResponse get(@PathVariable Long appointmentId) {
        return appointmentService.getById(SecurityUtils.currentUser(), appointmentId);
    }

    @PatchMapping("/{appointmentId}/cancel")
    public AppointmentResponse cancel(
            @PathVariable Long appointmentId,
            @RequestBody(required = false) @Valid StatusChangeRequest request
    ) {
        String reason = request == null ? null : request.getReason();
        return appointmentService.cancel(SecurityUtils.currentUser(), appointmentId, reason);
    }

    @PatchMapping("/{appointmentId}/confirm")
    public AppointmentResponse confirm(@PathVariable Long appointmentId) {
        return appointmentService.confirm(SecurityUtils.currentUser(), appointmentId);
    }

    @PatchMapping("/{appointmentId}/reject")
    public AppointmentResponse reject(
            @PathVariable Long appointmentId,
            @RequestBody(required = false) @Valid StatusChangeRequest request
    ) {
        String reason = request == null ? null : request.getReason();
        return appointmentService.reject(SecurityUtils.currentUser(), appointmentId, reason);
    }

    @PatchMapping("/{appointmentId}/complete")
    public AppointmentResponse complete(@PathVariable Long appointmentId) {
        return appointmentService.complete(SecurityUtils.currentUser(), appointmentId);
    }
}
