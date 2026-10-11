package com.medicurex.profile.service;

import com.medicurex.profile.dto.AdminDirectoryResponse;
import com.medicurex.profile.dto.AdminUserProfilesResponse;
import com.medicurex.profile.dto.DoctorProfileResponse;
import com.medicurex.profile.dto.PatientProfileResponse;
import com.medicurex.profile.exception.ApiException;
import com.medicurex.profile.repository.DoctorProfileRepository;
import com.medicurex.profile.repository.PatientProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminProfileService {

    private final PatientProfileRepository patientProfileRepository;
    private final DoctorProfileRepository doctorProfileRepository;

    public AdminProfileService(
            PatientProfileRepository patientProfileRepository,
            DoctorProfileRepository doctorProfileRepository
    ) {
        this.patientProfileRepository = patientProfileRepository;
        this.doctorProfileRepository = doctorProfileRepository;
    }

    @Transactional(readOnly = true)
    public AdminDirectoryResponse listAll() {
        List<PatientProfileResponse> patients = patientProfileRepository.findAll().stream()
                .map(PatientProfileResponse::from)
                .toList();
        List<DoctorProfileResponse> doctors = doctorProfileRepository.findAllWithSlots().stream()
                .map(DoctorProfileResponse::from)
                .toList();
        return new AdminDirectoryResponse(patients, doctors);
    }

    @Transactional(readOnly = true)
    public AdminUserProfilesResponse getByAuthUserId(Long authUserId) {
        PatientProfileResponse patient = patientProfileRepository.findByAuthUserId(authUserId)
                .map(PatientProfileResponse::from)
                .orElse(null);
        DoctorProfileResponse doctor = doctorProfileRepository.findByAuthUserId(authUserId)
                .map(DoctorProfileResponse::from)
                .orElse(null);

        if (patient == null && doctor == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "No profile found for the requested user");
        }

        return new AdminUserProfilesResponse(patient, doctor);
    }
}
