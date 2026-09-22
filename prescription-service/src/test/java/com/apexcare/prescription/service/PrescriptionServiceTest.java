package com.apexcare.prescription.service;

import com.apexcare.prescription.client.AppointmentClient;
import com.apexcare.prescription.client.AppointmentSnapshot;
import com.apexcare.prescription.client.MedicineSnapshot;
import com.apexcare.prescription.client.PharmacyClient;
import com.apexcare.prescription.dto.CreatePrescriptionRequest;
import com.apexcare.prescription.dto.PrescriptionItemRequest;
import com.apexcare.prescription.entity.Prescription;
import com.apexcare.prescription.exception.ApiException;
import com.apexcare.prescription.repository.PrescriptionRepository;
import com.apexcare.prescription.security.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrescriptionServiceTest {

    @Mock
    private PrescriptionRepository prescriptionRepository;

    @Mock
    private AppointmentClient appointmentClient;

    @Mock
    private PharmacyClient pharmacyClient;

    private PrescriptionService prescriptionService;

    @BeforeEach
    void setUp() {
        prescriptionService = new PrescriptionService(prescriptionRepository, appointmentClient, pharmacyClient);
    }

    @Test
    void createsPrescriptionFromAppointmentAndPharmacyName() {
        AuthenticatedUser doctor = new AuthenticatedUser(21L, "doctor@gmail.com", "doctor");
        when(appointmentClient.getAppointment(1L, "Bearer token")).thenReturn(appointment(1L, "COMPLETED"));
        when(prescriptionRepository.existsByAppointmentId(1L)).thenReturn(false);
        MedicineSnapshot medicine = new MedicineSnapshot();
        medicine.setId(9L);
        medicine.setName("Paracetamol");
        when(pharmacyClient.getMedicine(9L, "Bearer token")).thenReturn(medicine);
        when(prescriptionRepository.save(any(Prescription.class))).thenAnswer(invocation -> {
            Prescription prescription = invocation.getArgument(0);
            prescription.setId(4L);
            prescription.getItems().getFirst().setId(7L);
            return prescription;
        });

        var response = prescriptionService.create(doctor, "Bearer token", request(1L, 9L, "Ignored name"));

        assertThat(response.getId()).isEqualTo(4L);
        assertThat(response.getPatientId()).isEqualTo(11L);
        assertThat(response.getDoctorId()).isEqualTo(21L);
        assertThat(response.getItems().getFirst().getMedicineName()).isEqualTo("Paracetamol");
        assertThat(response.getItems().getFirst().getMedicineId()).isEqualTo(9L);

        ArgumentCaptor<Prescription> captor = ArgumentCaptor.forClass(Prescription.class);
        verify(prescriptionRepository).save(captor.capture());
        assertThat(captor.getValue().getPatientId()).isEqualTo(11L);
    }

    @Test
    void rejectsOtherDoctorsAppointment() {
        AuthenticatedUser otherDoctor = new AuthenticatedUser(22L, "otherdoc@gmail.com", "doctor");
        when(appointmentClient.getAppointment(1L, "Bearer token")).thenReturn(appointment(1L, "COMPLETED"));

        assertThatThrownBy(() -> prescriptionService.create(otherDoctor, "Bearer token", request(1L, null, "Amoxicillin")))
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> assertThat(((ApiException) exception).getStatus()).isEqualTo(HttpStatus.FORBIDDEN));
        verify(prescriptionRepository, never()).save(any());
    }

    @Test
    void rejectsPendingAppointment() {
        AuthenticatedUser doctor = new AuthenticatedUser(21L, "doctor@gmail.com", "doctor");
        when(appointmentClient.getAppointment(1L, "Bearer token")).thenReturn(appointment(1L, "PENDING"));

        assertThatThrownBy(() -> prescriptionService.create(doctor, "Bearer token", request(1L, null, "Amoxicillin")))
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> assertThat(((ApiException) exception).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void rejectsDuplicatePrescriptionForSameAppointment() {
        AuthenticatedUser doctor = new AuthenticatedUser(21L, "doctor@gmail.com", "doctor");
        when(appointmentClient.getAppointment(1L, "Bearer token")).thenReturn(appointment(1L, "CONFIRMED"));
        when(prescriptionRepository.existsByAppointmentId(1L)).thenReturn(true);

        assertThatThrownBy(() -> prescriptionService.create(doctor, "Bearer token", request(1L, null, "Amoxicillin")))
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> assertThat(((ApiException) exception).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    private static AppointmentSnapshot appointment(Long id, String status) {
        AppointmentSnapshot snapshot = new AppointmentSnapshot();
        snapshot.setId(id);
        snapshot.setPatientId(11L);
        snapshot.setDoctorId(21L);
        snapshot.setStatus(status);
        return snapshot;
    }

    private static CreatePrescriptionRequest request(Long appointmentId, Long medicineId, String medicineName) {
        PrescriptionItemRequest item = new PrescriptionItemRequest();
        item.setMedicineId(medicineId);
        item.setMedicineName(medicineName);
        item.setDosage("500mg");
        item.setFrequency("Twice daily");
        item.setDuration("5 days");
        CreatePrescriptionRequest request = new CreatePrescriptionRequest();
        request.setAppointmentId(appointmentId);
        request.setDiagnosis("Fever");
        request.setItems(List.of(item));
        return request;
    }
}
