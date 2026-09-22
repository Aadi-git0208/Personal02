package com.apexcare.appointment.controller;

import com.apexcare.appointment.dto.AppointmentResponse;
import com.apexcare.appointment.dto.CreateAppointmentRequest;
import com.apexcare.appointment.security.AuthenticatedUser;
import com.apexcare.appointment.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public ResponseEntity<AppointmentResponse> create(
            Authentication authentication,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @Valid @RequestBody CreateAppointmentRequest request
    ) {
        AuthenticatedUser user = AuthenticatedUser.from(authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(appointmentService.create(user, authorization, request));
    }

    @GetMapping("/patient/me")
    public ResponseEntity<List<AppointmentResponse>> myPatientAppointments(Authentication authentication) {
        AuthenticatedUser user = AuthenticatedUser.from(authentication);
        return ResponseEntity.ok(appointmentService.listMineAsPatient(user.getUserId()));
    }

    @GetMapping("/doctor/me")
    public ResponseEntity<List<AppointmentResponse>> myDoctorAppointments(Authentication authentication) {
        AuthenticatedUser user = AuthenticatedUser.from(authentication);
        return ResponseEntity.ok(appointmentService.listMineAsDoctor(user.getUserId()));
    }

    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> allAppointments() {
        return ResponseEntity.ok(appointmentService.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponse> get(Authentication authentication, @PathVariable Long id) {
        AuthenticatedUser user = AuthenticatedUser.from(authentication);
        return ResponseEntity.ok(appointmentService.getById(user, id));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<AppointmentResponse> cancel(Authentication authentication, @PathVariable Long id) {
        AuthenticatedUser user = AuthenticatedUser.from(authentication);
        return ResponseEntity.ok(appointmentService.cancel(user.getUserId(), id));
    }

    @PatchMapping("/{id}/accept")
    public ResponseEntity<AppointmentResponse> accept(Authentication authentication, @PathVariable Long id) {
        AuthenticatedUser user = AuthenticatedUser.from(authentication);
        return ResponseEntity.ok(appointmentService.accept(user.getUserId(), id));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<AppointmentResponse> reject(Authentication authentication, @PathVariable Long id) {
        AuthenticatedUser user = AuthenticatedUser.from(authentication);
        return ResponseEntity.ok(appointmentService.reject(user.getUserId(), id));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<AppointmentResponse> complete(Authentication authentication, @PathVariable Long id) {
        AuthenticatedUser user = AuthenticatedUser.from(authentication);
        return ResponseEntity.ok(appointmentService.complete(user.getUserId(), id));
    }
}
