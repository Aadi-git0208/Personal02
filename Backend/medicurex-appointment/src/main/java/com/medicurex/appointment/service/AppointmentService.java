package com.medicurex.appointment.service;

import com.medicurex.appointment.client.ApprovedDoctorDirectory;
import com.medicurex.appointment.client.ProfileDirectoryService;
import com.medicurex.appointment.dto.AppointmentResponse;
import com.medicurex.appointment.dto.CreateAppointmentRequest;
import com.medicurex.appointment.dto.DoctorAvailabilityResponse;
import com.medicurex.appointment.exception.ApiException;
import com.medicurex.appointment.model.Appointment;
import com.medicurex.appointment.model.AppointmentStatus;
import com.medicurex.appointment.repository.AppointmentRepository;
import com.medicurex.appointment.security.AuthenticatedUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
public class AppointmentService {

    private static final Set<AppointmentStatus> ACTIVE_STATUSES =
            EnumSet.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED);

    private final AppointmentRepository appointmentRepository;
    private final ProfileDirectoryService profileDirectoryService;
    private final Clock clock;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            ProfileDirectoryService profileDirectoryService,
            Clock clock
    ) {
        this.appointmentRepository = appointmentRepository;
        this.profileDirectoryService = profileDirectoryService;
        this.clock = clock;
    }

    @Transactional
    public AppointmentResponse create(AuthenticatedUser user, CreateAppointmentRequest request) {
        validateTimeRange(request.getAppointmentDate(), request.getStartTime(), request.getEndTime());

        ApprovedDoctorDirectory doctor = profileDirectoryService.requireApprovedDoctor(request.getDoctorProfileId());
        if (doctor.getAuthUserId().equals(user.getUserId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "You cannot book an appointment with yourself");
        }

        validateAdvertisedSlot(request.getSlotLabel(), doctor.getAvailabilitySlots());

        List<Appointment> doctorOverlaps = appointmentRepository.lockDoctorOverlaps(
                doctor.getAuthUserId(),
                request.getAppointmentDate(),
                request.getStartTime(),
                request.getEndTime(),
                ACTIVE_STATUSES
        );
        if (!doctorOverlaps.isEmpty()) {
            throw new ApiException(HttpStatus.CONFLICT, "This doctor already has an appointment in that time range");
        }

        List<Appointment> patientOverlaps = appointmentRepository.lockPatientOverlaps(
                user.getUserId(),
                request.getAppointmentDate(),
                request.getStartTime(),
                request.getEndTime(),
                ACTIVE_STATUSES
        );
        if (!patientOverlaps.isEmpty()) {
            throw new ApiException(HttpStatus.CONFLICT, "You already have an appointment in that time range");
        }

        Appointment appointment = new Appointment();
        appointment.setPatientUserId(user.getUserId());
        appointment.setDoctorUserId(doctor.getAuthUserId());
        appointment.setDoctorProfileId(doctor.getId());
        appointment.setDoctorFullName(doctor.getFullName());
        appointment.setDoctorSpecialization(doctor.getSpecialization());
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setStartTime(request.getStartTime());
        appointment.setEndTime(request.getEndTime());
        appointment.setSlotLabel(normalize(request.getSlotLabel()));
        appointment.setReason(request.getReason().trim());
        appointment.setNotes(normalize(request.getNotes()));
        appointment.setStatus(AppointmentStatus.PENDING);

        return AppointmentResponse.from(appointmentRepository.save(appointment), true);
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> listMineAsPatient(AuthenticatedUser user, Pageable pageable) {
        return appointmentRepository
                .findByPatientUserIdOrderByAppointmentDateAscStartTimeAsc(user.getUserId(), pageable)
                .map(appointment -> AppointmentResponse.from(appointment, true));
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> listMineAsDoctor(AuthenticatedUser user, Pageable pageable) {
        return appointmentRepository
                .findByDoctorUserIdOrderByAppointmentDateAscStartTimeAsc(user.getUserId(), pageable)
                .map(appointment -> AppointmentResponse.from(appointment, true));
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> listAllForAdmin(Pageable pageable) {
        return appointmentRepository
                .findAllByOrderByAppointmentDateAscStartTimeAsc(pageable)
                .map(appointment -> AppointmentResponse.from(appointment, true));
    }

    @Transactional(readOnly = true)
    public AppointmentResponse getById(AuthenticatedUser user, Long appointmentId) {
        Appointment appointment = require(appointmentId);
        assertCanView(user, appointment);
        return AppointmentResponse.from(appointment, canSeeNotes(user, appointment));
    }

    @Transactional
    public AppointmentResponse cancel(AuthenticatedUser user, Long appointmentId, String reason) {
        Appointment appointment = require(appointmentId);
        if (!user.isAdmin() && !appointment.getPatientUserId().equals(user.getUserId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access denied");
        }
        transition(appointment, AppointmentStatus.CANCELLED);
        appointment.setCancellationReason(normalize(reason));
        return AppointmentResponse.from(appointmentRepository.save(appointment), canSeeNotes(user, appointment));
    }

    @Transactional
    public AppointmentResponse confirm(AuthenticatedUser user, Long appointmentId) {
        Appointment appointment = requireAssignedDoctorOrAdmin(user, appointmentId);
        transition(appointment, AppointmentStatus.CONFIRMED);
        return AppointmentResponse.from(appointmentRepository.save(appointment), true);
    }

    @Transactional
    public AppointmentResponse reject(AuthenticatedUser user, Long appointmentId, String reason) {
        Appointment appointment = requireAssignedDoctorOrAdmin(user, appointmentId);
        transition(appointment, AppointmentStatus.REJECTED);
        appointment.setCancellationReason(normalize(reason));
        return AppointmentResponse.from(appointmentRepository.save(appointment), true);
    }

    @Transactional
    public AppointmentResponse complete(AuthenticatedUser user, Long appointmentId) {
        Appointment appointment = requireAssignedDoctorOrAdmin(user, appointmentId);
        transition(appointment, AppointmentStatus.COMPLETED);
        return AppointmentResponse.from(appointmentRepository.save(appointment), true);
    }

    @Transactional(readOnly = true)
    public DoctorAvailabilityResponse availability(Long doctorProfileId, LocalDate date) {
        if (date == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Date is required");
        }

        ApprovedDoctorDirectory doctor = profileDirectoryService.requireApprovedDoctor(doctorProfileId);
        List<DoctorAvailabilityResponse.BookedRange> booked = appointmentRepository
                .findByDoctorUserIdAndAppointmentDateAndStatusIn(doctor.getAuthUserId(), date, ACTIVE_STATUSES)
                .stream()
                .map(appointment -> new DoctorAvailabilityResponse.BookedRange(
                        appointment.getStartTime(),
                        appointment.getEndTime(),
                        appointment.getStatus().name()
                ))
                .toList();

        List<String> advertised = doctor.getAvailabilitySlots() == null
                ? List.of()
                : List.copyOf(doctor.getAvailabilitySlots());

        return new DoctorAvailabilityResponse(
                doctor.getId(),
                doctor.getAuthUserId(),
                date,
                advertised,
                booked
        );
    }

    private void validateTimeRange(LocalDate date, LocalTime startTime, LocalTime endTime) {
        if (!endTime.isAfter(startTime)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "End time must be after start time");
        }

        LocalDateTime start = LocalDateTime.of(date, startTime);
        if (!start.isAfter(LocalDateTime.now(clock))) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Appointment time cannot be in the past");
        }
    }

    private void validateAdvertisedSlot(String slotLabel, List<String> advertisedSlots) {
        if (slotLabel == null || slotLabel.isBlank() || advertisedSlots == null || advertisedSlots.isEmpty()) {
            return;
        }

        boolean matches = advertisedSlots.stream()
                .anyMatch(slot -> slot != null && slot.trim().equalsIgnoreCase(slotLabel.trim()));
        if (!matches) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "The selected slot is not in this doctor's advertised availability"
            );
        }
    }

    private void transition(Appointment appointment, AppointmentStatus target) {
        AppointmentStatus current = appointment.getStatus();
        boolean allowed = switch (target) {
            case CONFIRMED, REJECTED -> current == AppointmentStatus.PENDING;
            case CANCELLED -> current == AppointmentStatus.PENDING || current == AppointmentStatus.CONFIRMED;
            case COMPLETED -> current == AppointmentStatus.CONFIRMED;
            case PENDING -> false;
        };

        if (!allowed) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Cannot change appointment status from " + current + " to " + target
            );
        }
        appointment.setStatus(target);
    }

    private Appointment requireAssignedDoctorOrAdmin(AuthenticatedUser user, Long appointmentId) {
        Appointment appointment = require(appointmentId);
        if (user.isAdmin() || (user.isDoctor() && appointment.getDoctorUserId().equals(user.getUserId()))) {
            return appointment;
        }
        throw new ApiException(HttpStatus.FORBIDDEN, "Access denied");
    }

    private void assertCanView(AuthenticatedUser user, Appointment appointment) {
        if (user.isAdmin()) {
            return;
        }
        if (user.isPatient() && appointment.getPatientUserId().equals(user.getUserId())) {
            return;
        }
        if (user.isDoctor() && appointment.getDoctorUserId().equals(user.getUserId())) {
            return;
        }
        throw new ApiException(HttpStatus.FORBIDDEN, "Access denied");
    }

    private boolean canSeeNotes(AuthenticatedUser user, Appointment appointment) {
        return user.isAdmin()
                || appointment.getPatientUserId().equals(user.getUserId())
                || appointment.getDoctorUserId().equals(user.getUserId());
    }

    private Appointment require(Long appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Appointment not found"));
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
