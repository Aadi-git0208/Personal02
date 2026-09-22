package com.apexcare.appointment.service;

import com.apexcare.appointment.client.DoctorProfileSnapshot;
import com.apexcare.appointment.client.ProfileClient;
import com.apexcare.appointment.dto.AppointmentResponse;
import com.apexcare.appointment.dto.CreateAppointmentRequest;
import com.apexcare.appointment.entity.Appointment;
import com.apexcare.appointment.entity.AppointmentStatus;
import com.apexcare.appointment.exception.ApiException;
import com.apexcare.appointment.repository.AppointmentRepository;
import com.apexcare.appointment.security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;

@Service
public class AppointmentService {

    private static final Set<AppointmentStatus> ACTIVE_STATUSES = Set.of(
            AppointmentStatus.PENDING,
            AppointmentStatus.CONFIRMED
    );

    private final AppointmentRepository appointmentRepository;
    private final ProfileClient profileClient;

    public AppointmentService(AppointmentRepository appointmentRepository, ProfileClient profileClient) {
        this.appointmentRepository = appointmentRepository;
        this.profileClient = profileClient;
    }

    @Transactional
    public AppointmentResponse create(AuthenticatedUser patient, String authorization, CreateAppointmentRequest request) {
        validateTimeRange(request.getStartTime(), request.getEndTime());

        DoctorProfileSnapshot doctor = profileClient.getDoctor(request.getDoctorId(), authorization);
        if (!doctor.isProfileCompleted() || doctor.getConsultationFee() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Doctor profile is not available for booking");
        }
        if (patient.getUserId().equals(doctor.getUserId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "A patient cannot create an appointment with themselves as doctor");
        }

        assertNoOverlap(doctor.getUserId(), request.getAppointmentDate(), request.getStartTime(), request.getEndTime());

        Appointment appointment = new Appointment();
        appointment.setPatientId(patient.getUserId());
        appointment.setDoctorId(doctor.getUserId());
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setStartTime(request.getStartTime());
        appointment.setEndTime(request.getEndTime());
        appointment.setConsultationFee(doctor.getConsultationFee());
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setReason(trimToNull(request.getReason()));

        return toResponse(appointmentRepository.save(appointment));
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> listMineAsPatient(Long patientId) {
        return appointmentRepository.findByPatientIdOrderByAppointmentDateDescStartTimeDesc(patientId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> listMineAsDoctor(Long doctorId) {
        return appointmentRepository.findByDoctorIdOrderByAppointmentDateDescStartTimeDesc(doctorId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> listAll() {
        return appointmentRepository.findAllByOrderByAppointmentDateDescStartTimeDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AppointmentResponse getById(AuthenticatedUser user, Long id) {
        Appointment appointment = requireAppointment(id);
        assertCanView(user, appointment);
        return toResponse(appointment);
    }

    @Transactional
    public AppointmentResponse cancel(Long patientId, Long id) {
        Appointment appointment = requireAppointment(id);
        if (!appointment.getPatientId().equals(patientId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only cancel your own appointments");
        }
        if (appointment.getStatus() != AppointmentStatus.PENDING
                && appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new ApiException(HttpStatus.CONFLICT, "Only pending or confirmed appointments can be cancelled");
        }
        appointment.setStatus(AppointmentStatus.CANCELLED);
        return toResponse(appointmentRepository.save(appointment));
    }

    @Transactional
    public AppointmentResponse accept(Long doctorId, Long id) {
        Appointment appointment = requireOwnedByDoctor(doctorId, id);
        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new ApiException(HttpStatus.CONFLICT, "Only pending appointments can be accepted");
        }
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        return toResponse(appointmentRepository.save(appointment));
    }

    @Transactional
    public AppointmentResponse reject(Long doctorId, Long id) {
        Appointment appointment = requireOwnedByDoctor(doctorId, id);
        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new ApiException(HttpStatus.CONFLICT, "Only pending appointments can be rejected");
        }
        appointment.setStatus(AppointmentStatus.REJECTED);
        return toResponse(appointmentRepository.save(appointment));
    }

    @Transactional
    public AppointmentResponse complete(Long doctorId, Long id) {
        Appointment appointment = requireOwnedByDoctor(doctorId, id);
        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new ApiException(HttpStatus.CONFLICT, "Only confirmed appointments can be completed");
        }
        appointment.setStatus(AppointmentStatus.COMPLETED);
        return toResponse(appointmentRepository.save(appointment));
    }

    private Appointment requireOwnedByDoctor(Long doctorId, Long id) {
        Appointment appointment = requireAppointment(id);
        if (!appointment.getDoctorId().equals(doctorId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only manage your own appointments");
        }
        return appointment;
    }

    private Appointment requireAppointment(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Appointment not found"));
    }

    private void assertCanView(AuthenticatedUser user, Appointment appointment) {
        if ("ADMIN".equals(user.getRole())) {
            return;
        }
        if ("PATIENT".equals(user.getRole()) && appointment.getPatientId().equals(user.getUserId())) {
            return;
        }
        if ("DOCTOR".equals(user.getRole()) && appointment.getDoctorId().equals(user.getUserId())) {
            return;
        }
        throw new ApiException(HttpStatus.FORBIDDEN, "Access denied");
    }

    private void validateTimeRange(java.time.LocalTime startTime, java.time.LocalTime endTime) {
        if (!startTime.isBefore(endTime)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "startTime must be before endTime");
        }
    }

    private void assertNoOverlap(
            Long doctorId,
            java.time.LocalDate date,
            java.time.LocalTime startTime,
            java.time.LocalTime endTime
    ) {
        List<Appointment> overlapping = appointmentRepository.findOverlapping(
                doctorId,
                date,
                startTime,
                endTime,
                ACTIVE_STATUSES
        );
        if (!overlapping.isEmpty()) {
            throw new ApiException(HttpStatus.CONFLICT, "Doctor already has a pending or confirmed appointment in this time range");
        }
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private AppointmentResponse toResponse(Appointment appointment) {
        AppointmentResponse response = new AppointmentResponse();
        response.setId(appointment.getId());
        response.setPatientId(appointment.getPatientId());
        response.setDoctorId(appointment.getDoctorId());
        response.setAppointmentDate(appointment.getAppointmentDate());
        response.setStartTime(appointment.getStartTime());
        response.setEndTime(appointment.getEndTime());
        response.setConsultationFee(appointment.getConsultationFee());
        response.setStatus(appointment.getStatus());
        response.setReason(appointment.getReason());
        response.setCreatedAt(appointment.getCreatedAt());
        response.setUpdatedAt(appointment.getUpdatedAt());
        return response;
    }
}
