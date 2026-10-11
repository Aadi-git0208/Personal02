package com.medicurex.profile.service;

import com.medicurex.profile.dto.PatientProfileRequest;
import com.medicurex.profile.dto.PatientProfileResponse;
import com.medicurex.profile.exception.ApiException;
import com.medicurex.profile.model.PatientProfile;
import com.medicurex.profile.repository.PatientProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatientProfileService {

    private final PatientProfileRepository patientProfileRepository;

    public PatientProfileService(PatientProfileRepository patientProfileRepository) {
        this.patientProfileRepository = patientProfileRepository;
    }

    @Transactional(readOnly = true)
    public PatientProfileResponse getMine(Long authUserId) {
        return PatientProfileResponse.from(requireByAuthUserId(authUserId));
    }

    @Transactional
    public UpsertResult upsertMine(Long authUserId, PatientProfileRequest request) {
        boolean created = false;
        PatientProfile profile = patientProfileRepository.findByAuthUserId(authUserId).orElse(null);
        if (profile == null) {
            profile = new PatientProfile();
            profile.setAuthUserId(authUserId);
            created = true;
        }

        apply(profile, request);
        PatientProfile saved = patientProfileRepository.save(profile);
        return new UpsertResult(created, PatientProfileResponse.from(saved));
    }

    @Transactional(readOnly = true)
    public PatientProfileResponse findByAuthUserId(Long authUserId) {
        return patientProfileRepository.findByAuthUserId(authUserId)
                .map(PatientProfileResponse::from)
                .orElse(null);
    }

    private PatientProfile requireByAuthUserId(Long authUserId) {
        return patientProfileRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Patient profile not found"));
    }

    private void apply(PatientProfile profile, PatientProfileRequest request) {
        profile.setFullName(request.getFullName().trim());
        profile.setPhoneNumber(request.getPhoneNumber().trim());
        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setGender(normalizeOptional(request.getGender()));
        profile.setAddress(normalizeOptional(request.getAddress()));
        profile.setBloodGroup(normalizeOptional(request.getBloodGroup()));
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    public record UpsertResult(boolean created, PatientProfileResponse profile) {
    }
}
