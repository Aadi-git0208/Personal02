package com.apexcare.profile.service;

import com.apexcare.profile.dto.DoctorAvailabilityRequest;
import com.apexcare.profile.dto.DoctorProfileRequest;
import com.apexcare.profile.entity.DoctorProfile;
import com.apexcare.profile.exception.ApiException;
import com.apexcare.profile.repository.DoctorAvailabilityRepository;
import com.apexcare.profile.repository.DoctorProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorProfileServiceTest {

    @Mock
    private DoctorProfileRepository doctorProfileRepository;

    @Mock
    private DoctorAvailabilityRepository doctorAvailabilityRepository;

    private DoctorProfileService doctorProfileService;

    @BeforeEach
    void setUp() {
        doctorProfileService = new DoctorProfileService(doctorProfileRepository, doctorAvailabilityRepository);
    }

    @Test
    void completedProfileRequiresSpecialization() {
        DoctorProfileRequest request = new DoctorProfileRequest();
        request.setFullName("Dr Sharma");
        request.setExperience(8);
        request.setConsultationFee(500);
        request.setProfileCompleted(true);

        when(doctorProfileRepository.findByUserId(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorProfileService.upsertMyProfile(7L, request))
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> {
                    ApiException apiException = (ApiException) exception;
                    assertThat(apiException.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(apiException.getMessage()).contains("specialization");
                });
    }

    @Test
    void availabilityRequiresStartBeforeEnd() {
        DoctorProfile profile = new DoctorProfile();
        profile.setId(3L);
        profile.setUserId(7L);
        when(doctorProfileRepository.findByUserId(7L)).thenReturn(Optional.of(profile));

        DoctorAvailabilityRequest request = new DoctorAvailabilityRequest();
        request.setDayOfWeek(DayOfWeek.MONDAY);
        request.setStartTime(LocalTime.of(14, 0));
        request.setEndTime(LocalTime.of(10, 0));

        assertThatThrownBy(() -> doctorProfileService.createAvailability(7L, request))
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> assertThat(((ApiException) exception).getStatus())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void overlappingAvailabilityIsRejected() {
        DoctorProfile profile = new DoctorProfile();
        profile.setId(3L);
        profile.setUserId(7L);
        when(doctorProfileRepository.findByUserId(7L)).thenReturn(Optional.of(profile));

        com.apexcare.profile.entity.DoctorAvailability existing = new com.apexcare.profile.entity.DoctorAvailability();
        existing.setId(11L);
        existing.setDayOfWeek(DayOfWeek.MONDAY);
        existing.setStartTime(LocalTime.of(10, 0));
        existing.setEndTime(LocalTime.of(12, 0));
        when(doctorAvailabilityRepository.findByDoctorProfileAndDayOfWeek(profile, DayOfWeek.MONDAY))
                .thenReturn(List.of(existing));

        DoctorAvailabilityRequest request = new DoctorAvailabilityRequest();
        request.setDayOfWeek(DayOfWeek.MONDAY);
        request.setStartTime(LocalTime.of(11, 0));
        request.setEndTime(LocalTime.of(13, 0));

        assertThatThrownBy(() -> doctorProfileService.createAvailability(7L, request))
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> assertThat(((ApiException) exception).getStatus())
                        .isEqualTo(HttpStatus.CONFLICT));
    }
}
