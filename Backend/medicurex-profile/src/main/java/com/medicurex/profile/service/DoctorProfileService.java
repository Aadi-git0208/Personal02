package com.medicurex.profile.service;

import com.medicurex.profile.dto.DoctorProfileRequest;
import com.medicurex.profile.dto.DoctorProfileResponse;
import com.medicurex.profile.dto.PublicDoctorDirectoryResponse;
import com.medicurex.profile.exception.ApiException;
import com.medicurex.profile.model.ApprovalStatus;
import com.medicurex.profile.model.DoctorProfile;
import com.medicurex.profile.repository.DoctorProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DoctorProfileService {

    private final DoctorProfileRepository doctorProfileRepository;

    public DoctorProfileService(DoctorProfileRepository doctorProfileRepository) {
        this.doctorProfileRepository = doctorProfileRepository;
    }

    @Transactional(readOnly = true)
    public DoctorProfileResponse getMine(Long authUserId) {
        return DoctorProfileResponse.from(requireByAuthUserId(authUserId));
    }

    @Transactional
    public UpsertResult upsertMine(Long authUserId, DoctorProfileRequest request) {
        boolean created = false;
        DoctorProfile profile = doctorProfileRepository.findByAuthUserId(authUserId).orElse(null);
        if (profile == null) {
            profile = new DoctorProfile();
            profile.setAuthUserId(authUserId);
            profile.setApprovalStatus(ApprovalStatus.PENDING);
            created = true;
        }

        apply(profile, request);
        profile.setProfileCompleted(true);
        DoctorProfile saved = doctorProfileRepository.save(profile);
        return new UpsertResult(created, DoctorProfileResponse.from(saved));
    }

    @Transactional(readOnly = true)
    public List<PublicDoctorDirectoryResponse> publicDirectory() {
        return doctorProfileRepository.findByApprovalStatusOrderByFullNameAsc(ApprovalStatus.APPROVED).stream()
                .map(PublicDoctorDirectoryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PublicDoctorDirectoryResponse publicById(Long doctorProfileId) {
        return doctorProfileRepository.findByIdAndApprovalStatus(doctorProfileId, ApprovalStatus.APPROVED)
                .map(PublicDoctorDirectoryResponse::from)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Doctor profile not found"));
    }

    @Transactional(readOnly = true)
    public DoctorProfileResponse findByAuthUserId(Long authUserId) {
        return doctorProfileRepository.findByAuthUserId(authUserId)
                .map(DoctorProfileResponse::from)
                .orElse(null);
    }

    @Transactional
    public DoctorProfileResponse updateApproval(Long authUserId, ApprovalStatus status) {
        if (status == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Approval status is required");
        }

        DoctorProfile profile = requireByAuthUserId(authUserId);
        profile.setApprovalStatus(status);
        return DoctorProfileResponse.from(doctorProfileRepository.save(profile));
    }

    private DoctorProfile requireByAuthUserId(Long authUserId) {
        return doctorProfileRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Doctor profile not found"));
    }

    private void apply(DoctorProfile profile, DoctorProfileRequest request) {
        profile.setFullName(request.getFullName().trim());
        profile.setSpecialization(request.getSpecialization().trim());
        profile.setQualifications(request.getQualifications().trim());
        profile.setExperienceYears(request.getExperienceYears());
        profile.setConsultationFee(request.getConsultationFee());
        profile.setClinicAddress(request.getClinicAddress().trim());
        profile.setPhoneNumber(request.getPhoneNumber().trim());
        profile.setBiography(normalizeOptional(request.getBiography()));
        List<String> slots = request.getAvailabilitySlots() == null
                ? List.of()
                : request.getAvailabilitySlots().stream()
                .filter(slot -> slot != null && !slot.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
        profile.setAvailabilitySlots(slots);
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    public record UpsertResult(boolean created, DoctorProfileResponse profile) {
    }
}
