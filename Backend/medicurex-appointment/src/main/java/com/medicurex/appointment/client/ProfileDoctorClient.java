package com.medicurex.appointment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "profile-service", contextId = "profileDoctorClient")
public interface ProfileDoctorClient {

    @GetMapping("/api/profiles/doctor/directory/{doctorId}")
    ApprovedDoctorDirectory getApprovedDoctor(@PathVariable("doctorId") Long doctorProfileId);
}
