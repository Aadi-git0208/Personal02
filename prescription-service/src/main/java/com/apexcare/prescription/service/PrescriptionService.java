package com.apexcare.prescription.service;

import com.apexcare.prescription.client.AppointmentClient;
import com.apexcare.prescription.client.AppointmentSnapshot;
import com.apexcare.prescription.client.MedicineSnapshot;
import com.apexcare.prescription.client.PharmacyClient;
import com.apexcare.prescription.dto.CreatePrescriptionRequest;
import com.apexcare.prescription.dto.PrescriptionItemRequest;
import com.apexcare.prescription.dto.PrescriptionItemResponse;
import com.apexcare.prescription.dto.PrescriptionResponse;
import com.apexcare.prescription.entity.Prescription;
import com.apexcare.prescription.entity.PrescriptionItem;
import com.apexcare.prescription.exception.ApiException;
import com.apexcare.prescription.repository.PrescriptionRepository;
import com.apexcare.prescription.security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final AppointmentClient appointmentClient;
    private final PharmacyClient pharmacyClient;

    public PrescriptionService(
            PrescriptionRepository prescriptionRepository,
            AppointmentClient appointmentClient,
            PharmacyClient pharmacyClient
    ) {
        this.prescriptionRepository = prescriptionRepository;
        this.appointmentClient = appointmentClient;
        this.pharmacyClient = pharmacyClient;
    }

    @Transactional
    public PrescriptionResponse create(AuthenticatedUser user, String authorization, CreatePrescriptionRequest request) {
        AppointmentSnapshot appointment = appointmentClient.getAppointment(request.getAppointmentId(), authorization);
        if (!user.getUserId().equals(appointment.getDoctorId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only prescribe for your own appointment");
        }
        if (!appointment.allowsPrescription()) {
            throw new ApiException(HttpStatus.CONFLICT, "Prescriptions can only be written for confirmed or completed appointments");
        }
        if (prescriptionRepository.existsByAppointmentId(appointment.getId())) {
            throw new ApiException(HttpStatus.CONFLICT, "A prescription already exists for this appointment");
        }

        Prescription prescription = new Prescription();
        prescription.setAppointmentId(appointment.getId());
        prescription.setDoctorId(appointment.getDoctorId());
        prescription.setPatientId(appointment.getPatientId());
        prescription.setDiagnosis(request.getDiagnosis().trim());
        prescription.setNotes(trimToNull(request.getNotes()));
        for (PrescriptionItemRequest itemRequest : request.getItems()) {
            prescription.addItem(toItem(itemRequest, authorization));
        }
        return toResponse(prescriptionRepository.save(prescription));
    }

    @Transactional(readOnly = true)
    public List<PrescriptionResponse> listForDoctor(AuthenticatedUser user) {
        return prescriptionRepository.findGraphByDoctorId(user.getUserId()).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<PrescriptionResponse> listForPatient(AuthenticatedUser user) {
        return prescriptionRepository.findGraphByPatientId(user.getUserId()).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<PrescriptionResponse> listAll() {
        return prescriptionRepository.findAllGraph().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PrescriptionResponse get(AuthenticatedUser user, Long id) {
        Prescription prescription = prescriptionRepository.findGraphById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Prescription not found"));
        assertCanRead(user, prescription);
        return toResponse(prescription);
    }

    private PrescriptionItem toItem(PrescriptionItemRequest request, String authorization) {
        Long medicineId = request.getMedicineId();
        String medicineName = trimToNull(request.getMedicineName());
        if (medicineId != null) {
            MedicineSnapshot medicine = pharmacyClient.getMedicine(medicineId, authorization);
            if (StringUtils.hasText(medicine.getName())) {
                medicineName = medicine.getName().trim();
            }
        }
        if (!StringUtils.hasText(medicineName)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Each item needs a medicine name or a valid medicine id");
        }

        PrescriptionItem item = new PrescriptionItem();
        item.setMedicineId(medicineId);
        item.setMedicineName(medicineName);
        item.setDosage(request.getDosage().trim());
        item.setFrequency(request.getFrequency().trim());
        item.setDuration(request.getDuration().trim());
        item.setInstructions(trimToNull(request.getInstructions()));
        return item;
    }

    private void assertCanRead(AuthenticatedUser user, Prescription prescription) {
        if ("ADMIN".equals(user.getRole())) {
            return;
        }
        if ("DOCTOR".equals(user.getRole()) && user.getUserId().equals(prescription.getDoctorId())) {
            return;
        }
        if ("PATIENT".equals(user.getRole()) && user.getUserId().equals(prescription.getPatientId())) {
            return;
        }
        throw new ApiException(HttpStatus.FORBIDDEN, "Access denied");
    }

    private PrescriptionResponse toResponse(Prescription prescription) {
        PrescriptionResponse response = new PrescriptionResponse();
        response.setId(prescription.getId());
        response.setAppointmentId(prescription.getAppointmentId());
        response.setDoctorId(prescription.getDoctorId());
        response.setPatientId(prescription.getPatientId());
        response.setDiagnosis(prescription.getDiagnosis());
        response.setNotes(prescription.getNotes());
        response.setCreatedAt(prescription.getCreatedAt());
        response.setUpdatedAt(prescription.getUpdatedAt());
        response.setItems(prescription.getItems().stream().map(item -> toItemResponse(prescription.getId(), item)).toList());
        return response;
    }

    private PrescriptionItemResponse toItemResponse(Long prescriptionId, PrescriptionItem item) {
        PrescriptionItemResponse response = new PrescriptionItemResponse();
        response.setId(item.getId());
        response.setPrescriptionId(prescriptionId);
        response.setMedicineId(item.getMedicineId());
        response.setMedicineName(item.getMedicineName());
        response.setDosage(item.getDosage());
        response.setFrequency(item.getFrequency());
        response.setDuration(item.getDuration());
        response.setInstructions(item.getInstructions());
        return response;
    }

    private static String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
