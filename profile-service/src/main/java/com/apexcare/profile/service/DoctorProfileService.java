package com.apexcare.profile.service;

import com.apexcare.profile.dto.DoctorAvailabilityRequest;
import com.apexcare.profile.dto.DoctorAvailabilityResponse;
import com.apexcare.profile.dto.DoctorProfileRequest;
import com.apexcare.profile.dto.DoctorProfileResponse;
import com.apexcare.profile.entity.DoctorAvailability;
import com.apexcare.profile.entity.DoctorProfile;
import com.apexcare.profile.exception.ApiException;
import com.apexcare.profile.repository.DoctorAvailabilityRepository;
import com.apexcare.profile.repository.DoctorProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class DoctorProfileService {

    private final DoctorProfileRepository doctorProfileRepository;
    private final DoctorAvailabilityRepository doctorAvailabilityRepository;

    public DoctorProfileService(
            DoctorProfileRepository doctorProfileRepository,
            DoctorAvailabilityRepository doctorAvailabilityRepository
    ) {
        this.doctorProfileRepository = doctorProfileRepository;
        this.doctorAvailabilityRepository = doctorAvailabilityRepository;
    }

    @Transactional(readOnly = true)
    public DoctorProfileResponse getMyProfile(Long userId) {
        DoctorProfile profile = doctorProfileRepository.findGraphByUserId(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Doctor profile not found"));
        return toResponse(profile, true);
    }

    @Transactional
    public DoctorProfileResponse upsertMyProfile(Long userId, DoctorProfileRequest request) {
        if (request.getConsultationFee() != null && request.getConsultationFee() < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Consultation fee cannot be negative");
        }

        DoctorProfile profile = doctorProfileRepository.findByUserId(userId).orElseGet(DoctorProfile::new);
        profile.setUserId(userId);
        profile.setFullName(request.getFullName().trim());
        profile.setPhone(trimToNull(request.getPhone()));
        profile.setSpecialization(trimToNull(request.getSpecialization()));
        profile.setExperience(request.getExperience());
        profile.setConsultationFee(request.getConsultationFee());
        profile.setProfileImage(trimToNull(request.getProfileImage()));

        boolean completed = isComplete(profile);
        if (Boolean.TRUE.equals(request.getProfileCompleted()) && !completed) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "A completed doctor profile requires full name, specialization, experience and consultation fee"
            );
        }
        if (completed && !StringUtils.hasText(profile.getSpecialization())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Specialization cannot be blank for a completed doctor profile");
        }
        profile.setProfileCompleted(completed);

        DoctorProfile saved = doctorProfileRepository.save(profile);
        return toResponse(
                doctorProfileRepository.findGraphById(saved.getId()).orElse(saved),
                true
        );
    }

    @Transactional(readOnly = true)
    public List<DoctorProfileResponse> listDoctors(String specialization) {
        List<DoctorProfile> profiles;
        if (StringUtils.hasText(specialization)) {
            profiles = doctorProfileRepository.findByProfileCompletedTrueAndSpecializationContainingIgnoreCase(
                    specialization.trim()
            );
        } else {
            profiles = doctorProfileRepository.findByProfileCompletedTrue();
        }
        return profiles.stream().map(profile -> toResponse(profile, true)).toList();
    }

    @Transactional(readOnly = true)
    public DoctorProfileResponse getDoctor(Long doctorId) {
        DoctorProfile profile = doctorProfileRepository.findGraphById(doctorId)
                .filter(DoctorProfile::isProfileCompleted)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Doctor profile not found"));
        return toResponse(profile, true);
    }

    @Transactional(readOnly = true)
    public List<DoctorAvailabilityResponse> listMyAvailability(Long userId) {
        DoctorProfile profile = requireMyProfile(userId);
        return doctorAvailabilityRepository.findByDoctorProfileOrderByDayOfWeekAscStartTimeAsc(profile)
                .stream()
                .map(this::toAvailabilityResponse)
                .toList();
    }

    @Transactional
    public DoctorAvailabilityResponse createAvailability(Long userId, DoctorAvailabilityRequest request) {
        DoctorProfile profile = requireMyProfile(userId);
        validateTimeRange(request);
        assertNoOverlap(profile, request, null);

        DoctorAvailability availability = new DoctorAvailability();
        applyAvailability(availability, profile, request);
        return toAvailabilityResponse(doctorAvailabilityRepository.save(availability));
    }

    @Transactional
    public DoctorAvailabilityResponse updateAvailability(Long userId, Long availabilityId, DoctorAvailabilityRequest request) {
        DoctorProfile profile = requireMyProfile(userId);
        validateTimeRange(request);
        DoctorAvailability availability = doctorAvailabilityRepository.findByIdAndDoctorProfile(availabilityId, profile)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Availability slot not found"));
        assertNoOverlap(profile, request, availabilityId);
        applyAvailability(availability, profile, request);
        return toAvailabilityResponse(doctorAvailabilityRepository.save(availability));
    }

    @Transactional
    public void deleteAvailability(Long userId, Long availabilityId) {
        DoctorProfile profile = requireMyProfile(userId);
        DoctorAvailability availability = doctorAvailabilityRepository.findByIdAndDoctorProfile(availabilityId, profile)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Availability slot not found"));
        doctorAvailabilityRepository.delete(availability);
    }

    private DoctorProfile requireMyProfile(Long userId) {
        return doctorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Doctor profile not found"));
    }

    private void validateTimeRange(DoctorAvailabilityRequest request) {
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "startTime must be before endTime");
        }
    }

    private void assertNoOverlap(DoctorProfile profile, DoctorAvailabilityRequest request, Long excludeId) {
        List<DoctorAvailability> existing = doctorAvailabilityRepository.findByDoctorProfileAndDayOfWeek(
                profile,
                request.getDayOfWeek()
        );
        boolean overlaps = existing.stream()
                .filter(slot -> excludeId == null || !excludeId.equals(slot.getId()))
                .anyMatch(slot ->
                        request.getStartTime().isBefore(slot.getEndTime())
                                && request.getEndTime().isAfter(slot.getStartTime())
                );
        if (overlaps) {
            throw new ApiException(HttpStatus.CONFLICT, "Availability overlaps an existing slot on that day");
        }
    }

    private void applyAvailability(DoctorAvailability availability, DoctorProfile profile, DoctorAvailabilityRequest request) {
        availability.setDoctorProfile(profile);
        availability.setDayOfWeek(request.getDayOfWeek());
        availability.setStartTime(request.getStartTime());
        availability.setEndTime(request.getEndTime());
        availability.setAvailable(request.getAvailable() == null || request.getAvailable());
        if (availability.getId() == null && !profile.getAvailabilities().contains(availability)) {
            profile.getAvailabilities().add(availability);
        }
    }

    private boolean isComplete(DoctorProfile profile) {
        return StringUtils.hasText(profile.getFullName())
                && StringUtils.hasText(profile.getSpecialization())
                && profile.getExperience() != null
                && profile.getConsultationFee() != null;
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private DoctorProfileResponse toResponse(DoctorProfile profile, boolean includeAvailability) {
        DoctorProfileResponse response = new DoctorProfileResponse();
        response.setId(profile.getId());
        response.setUserId(profile.getUserId());
        response.setFullName(profile.getFullName());
        response.setPhone(profile.getPhone());
        response.setSpecialization(profile.getSpecialization());
        response.setExperience(profile.getExperience());
        response.setConsultationFee(profile.getConsultationFee());
        response.setProfileImage(profile.getProfileImage());
        response.setProfileCompleted(profile.isProfileCompleted());
        response.setCreatedAt(profile.getCreatedAt());
        response.setUpdatedAt(profile.getUpdatedAt());
        if (includeAvailability && profile.getAvailabilities() != null) {
            response.setAvailability(
                    profile.getAvailabilities().stream().map(this::toAvailabilityResponse).toList()
            );
        }
        return response;
    }

    private DoctorAvailabilityResponse toAvailabilityResponse(DoctorAvailability availability) {
        DoctorAvailabilityResponse response = new DoctorAvailabilityResponse();
        response.setId(availability.getId());
        response.setDoctorId(availability.getDoctorProfile().getId());
        response.setDayOfWeek(availability.getDayOfWeek());
        response.setStartTime(availability.getStartTime());
        response.setEndTime(availability.getEndTime());
        response.setAvailable(availability.isAvailable());
        return response;
    }
}
