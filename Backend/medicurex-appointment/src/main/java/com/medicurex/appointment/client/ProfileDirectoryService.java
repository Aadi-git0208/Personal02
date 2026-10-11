package com.medicurex.appointment.client;

import com.medicurex.appointment.exception.ApiException;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ProfileDirectoryService {

    private final ProfileDoctorClient profileDoctorClient;

    public ProfileDirectoryService(ProfileDoctorClient profileDoctorClient) {
        this.profileDoctorClient = profileDoctorClient;
    }

    public ApprovedDoctorDirectory requireApprovedDoctor(Long doctorProfileId) {
        try {
            ApprovedDoctorDirectory doctor = profileDoctorClient.getApprovedDoctor(doctorProfileId);
            if (doctor == null || doctor.getAuthUserId() == null) {
                throw new ApiException(
                        HttpStatus.BAD_GATEWAY,
                        "Profile Service returned an incomplete doctor directory entry"
                );
            }
            return doctor;
        } catch (ApiException exception) {
            throw exception;
        } catch (FeignException.NotFound exception) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Doctor is not available for booking");
        } catch (FeignException exception) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Profile Service is unavailable");
        } catch (RuntimeException exception) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Profile Service is unavailable");
        }
    }
}
