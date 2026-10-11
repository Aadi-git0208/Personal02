package com.medicurex.prescription.service;

import com.medicurex.prescription.client.AppointmentLookupService;
import com.medicurex.prescription.client.AppointmentSnapshot;
import com.medicurex.prescription.dto.CancelPrescriptionRequest;
import com.medicurex.prescription.dto.CreatePrescriptionRequest;
import com.medicurex.prescription.dto.PrescriptionItemRequest;
import com.medicurex.prescription.dto.PrescriptionResponse;
import com.medicurex.prescription.exception.ApiException;
import com.medicurex.prescription.model.Prescription;
import com.medicurex.prescription.model.PrescriptionItem;
import com.medicurex.prescription.model.PrescriptionStatus;
import com.medicurex.prescription.repository.PrescriptionRepository;
import com.medicurex.prescription.security.AuthenticatedUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final AppointmentLookupService appointmentLookupService;

    public PrescriptionService(
            PrescriptionRepository prescriptionRepository,
            AppointmentLookupService appointmentLookupService
    ) {
        this.prescriptionRepository = prescriptionRepository;
        this.appointmentLookupService = appointmentLookupService;
    }

    @Transactional
    public PrescriptionResponse create(AuthenticatedUser user, CreatePrescriptionRequest request) {
        if (!user.isDoctor()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only doctors can create prescriptions");
        }

        AppointmentSnapshot appointment = appointmentLookupService.requireEligibleAppointment(
                request.getAppointmentId(),
                user.getUserId()
        );

        if (prescriptionRepository.lockActiveByAppointmentId(appointment.getId(), PrescriptionStatus.ACTIVE).isPresent()) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "An active prescription already exists for this appointment"
            );
        }

        Prescription prescription = new Prescription();
        prescription.setPatientUserId(appointment.getPatientUserId());
        prescription.setDoctorUserId(appointment.getDoctorUserId());
        prescription.setAppointmentId(appointment.getId());
        prescription.setDiagnosis(request.getDiagnosis().trim());
        prescription.setInstructions(normalize(request.getInstructions()));
        prescription.setExpiresOn(request.getExpiresOn());
        prescription.setStatus(PrescriptionStatus.ACTIVE);

        for (PrescriptionItemRequest itemRequest : request.getItems()) {
            prescription.addItem(toItem(itemRequest));
        }

        return PrescriptionResponse.from(prescriptionRepository.save(prescription));
    }

    @Transactional(readOnly = true)
    public Page<PrescriptionResponse> listMineAsDoctor(AuthenticatedUser user, Pageable pageable) {
        if (!user.isDoctor()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return prescriptionRepository
                .findByDoctorUserIdOrderByCreatedAtDesc(user.getUserId(), pageable)
                .map(PrescriptionResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<PrescriptionResponse> listMineAsPatient(AuthenticatedUser user, Pageable pageable) {
        if (!user.isPatient()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return prescriptionRepository
                .findByPatientUserIdOrderByCreatedAtDesc(user.getUserId(), pageable)
                .map(PrescriptionResponse::from);
    }

    @Transactional(readOnly = true)
    public PrescriptionResponse getForDoctor(AuthenticatedUser user, Long prescriptionId) {
        Prescription prescription = requireWithItems(prescriptionId);
        if (!prescription.getDoctorUserId().equals(user.getUserId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return PrescriptionResponse.from(prescription);
    }

    @Transactional(readOnly = true)
    public PrescriptionResponse getForPatient(AuthenticatedUser user, Long prescriptionId) {
        Prescription prescription = requireWithItems(prescriptionId);
        if (!prescription.getPatientUserId().equals(user.getUserId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return PrescriptionResponse.from(prescription);
    }

    @Transactional(readOnly = true)
    public PrescriptionResponse get(AuthenticatedUser user, Long prescriptionId) {
        Prescription prescription = requireWithItems(prescriptionId);
        boolean author = prescription.getDoctorUserId().equals(user.getUserId());
        boolean owner = prescription.getPatientUserId().equals(user.getUserId());
        if (!author && !owner) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return PrescriptionResponse.from(prescription);
    }

    @Transactional
    public PrescriptionResponse cancel(AuthenticatedUser user, Long prescriptionId, CancelPrescriptionRequest request) {
        if (!user.isDoctor()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only the prescribing doctor can cancel a prescription");
        }

        Prescription prescription = requireWithItems(prescriptionId);
        if (!prescription.getDoctorUserId().equals(user.getUserId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access denied");
        }
        if (!prescription.isActive()) {
            throw new ApiException(HttpStatus.CONFLICT, "Prescription is already cancelled");
        }

        prescription.setStatus(PrescriptionStatus.CANCELLED);
        prescription.setCancelledAt(Instant.now());
        prescription.setCancellationReason(normalize(request == null ? null : request.getReason()));
        return PrescriptionResponse.from(prescriptionRepository.save(prescription));
    }

    private Prescription requireWithItems(Long prescriptionId) {
        return prescriptionRepository.findWithItemsById(prescriptionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Prescription not found"));
    }

    private PrescriptionItem toItem(PrescriptionItemRequest request) {
        PrescriptionItem item = new PrescriptionItem();
        item.setMedicineName(request.getMedicineName().trim());
        item.setDosage(request.getDosage().trim());
        item.setRoute(normalize(request.getRoute()));
        item.setFrequency(request.getFrequency().trim());
        item.setDuration(request.getDuration().trim());
        item.setQuantity(request.getQuantity());
        item.setInstructions(normalize(request.getInstructions()));
        return item;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
