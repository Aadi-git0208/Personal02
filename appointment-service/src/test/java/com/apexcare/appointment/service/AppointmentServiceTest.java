package com.apexcare.appointment.service;

import com.apexcare.appointment.client.DoctorProfileSnapshot;
import com.apexcare.appointment.client.ProfileClient;
import com.apexcare.appointment.dto.CreateAppointmentRequest;
import com.apexcare.appointment.entity.Appointment;
import com.apexcare.appointment.entity.AppointmentStatus;
import com.apexcare.appointment.exception.ApiException;
import com.apexcare.appointment.repository.AppointmentRepository;
import com.apexcare.appointment.security.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private ProfileClient profileClient;

    private AppointmentService appointmentService;

    @BeforeEach
    void setUp() {
        appointmentService = new AppointmentService(appointmentRepository, profileClient);
    }

    @Test
    void booksAppointmentUsingJwtPatientIdAndProfileFee() {
        AuthenticatedUser patient = new AuthenticatedUser(11L, "rahul@gmail.com", "patient");
        CreateAppointmentRequest request = request(3L, LocalTime.of(10, 0), LocalTime.of(10, 30));

        DoctorProfileSnapshot doctor = new DoctorProfileSnapshot();
        doctor.setId(3L);
        doctor.setUserId(21L);
        doctor.setConsultationFee(500);
        doctor.setProfileCompleted(true);
        when(profileClient.getDoctor(3L, "Bearer token")).thenReturn(doctor);
        when(appointmentRepository.findOverlapping(any(), any(), any(), any(), any())).thenReturn(List.of());
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
            Appointment appointment = invocation.getArgument(0);
            appointment.setId(9L);
            return appointment;
        });

        var response = appointmentService.create(patient, "Bearer token", request);

        assertThat(response.getPatientId()).isEqualTo(11L);
        assertThat(response.getDoctorId()).isEqualTo(21L);
        assertThat(response.getConsultationFee()).isEqualTo(500);
        assertThat(response.getStatus()).isEqualTo(AppointmentStatus.PENDING);

        ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentRepository).save(captor.capture());
        assertThat(captor.getValue().getPatientId()).isEqualTo(11L);
    }

    @Test
    void rejectsSelfBooking() {
        AuthenticatedUser patient = new AuthenticatedUser(21L, "doctor@gmail.com", "patient");
        DoctorProfileSnapshot doctor = new DoctorProfileSnapshot();
        doctor.setId(3L);
        doctor.setUserId(21L);
        doctor.setConsultationFee(500);
        doctor.setProfileCompleted(true);
        when(profileClient.getDoctor(eq(3L), any())).thenReturn(doctor);

        assertThatThrownBy(() -> appointmentService.create(patient, "Bearer token", request(3L, LocalTime.of(10, 0), LocalTime.of(11, 0))))
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> assertThat(((ApiException) exception).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void rejectsOverlappingActiveAppointments() {
        AuthenticatedUser patient = new AuthenticatedUser(11L, "rahul@gmail.com", "patient");
        DoctorProfileSnapshot doctor = new DoctorProfileSnapshot();
        doctor.setId(3L);
        doctor.setUserId(21L);
        doctor.setConsultationFee(500);
        doctor.setProfileCompleted(true);
        when(profileClient.getDoctor(eq(3L), any())).thenReturn(doctor);
        when(appointmentRepository.findOverlapping(any(), any(), any(), any(), any()))
                .thenReturn(List.of(new Appointment()));

        assertThatThrownBy(() -> appointmentService.create(patient, "Bearer token", request(3L, LocalTime.of(10, 0), LocalTime.of(11, 0))))
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> assertThat(((ApiException) exception).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    private CreateAppointmentRequest request(Long doctorId, LocalTime start, LocalTime end) {
        CreateAppointmentRequest request = new CreateAppointmentRequest();
        request.setDoctorId(doctorId);
        request.setAppointmentDate(LocalDate.now().plusDays(1));
        request.setStartTime(start);
        request.setEndTime(end);
        request.setReason("Checkup");
        return request;
    }
}
