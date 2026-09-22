package com.apexcare.profile.service;

import com.apexcare.profile.dto.PatientProfileRequest;
import com.apexcare.profile.dto.PatientProfileResponse;
import com.apexcare.profile.entity.PatientProfile;
import com.apexcare.profile.exception.ApiException;
import com.apexcare.profile.repository.PatientProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class PatientProfileService {

    private final PatientProfileRepository patientProfileRepository;

    public PatientProfileService(PatientProfileRepository patientProfileRepository) {
        this.patientProfileRepository = patientProfileRepository;
    }

    @Transactional(readOnly = true)
    public PatientProfileResponse getMyProfile(Long userId) {
        PatientProfile profile = patientProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Patient profile not found"));
        return toResponse(profile);
    }

    @Transactional
    public PatientProfileResponse upsertMyProfile(Long userId, PatientProfileRequest request) {
        PatientProfile profile = patientProfileRepository.findByUserId(userId).orElseGet(PatientProfile::new);
        profile.setUserId(userId);
        profile.setFullName(request.getFullName().trim());
        profile.setPhone(trimToNull(request.getPhone()));
        profile.setGender(trimToNull(request.getGender()));
        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setAddress(trimToNull(request.getAddress()));

        boolean completed = isComplete(profile);
        if (Boolean.TRUE.equals(request.getProfileCompleted()) && !completed) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "A completed patient profile requires full name, phone, gender and date of birth"
            );
        }
        profile.setProfileCompleted(completed);

        return toResponse(patientProfileRepository.save(profile));
    }

    private boolean isComplete(PatientProfile profile) {
        return StringUtils.hasText(profile.getFullName())
                && StringUtils.hasText(profile.getPhone())
                && StringUtils.hasText(profile.getGender())
                && profile.getDateOfBirth() != null;
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private PatientProfileResponse toResponse(PatientProfile profile) {
        PatientProfileResponse response = new PatientProfileResponse();
        response.setId(profile.getId());
        response.setUserId(profile.getUserId());
        response.setFullName(profile.getFullName());
        response.setPhone(profile.getPhone());
        response.setGender(profile.getGender());
        response.setDateOfBirth(profile.getDateOfBirth());
        response.setAddress(profile.getAddress());
        response.setProfileCompleted(profile.isProfileCompleted());
        response.setCreatedAt(profile.getCreatedAt());
        response.setUpdatedAt(profile.getUpdatedAt());
        return response;
    }
}
