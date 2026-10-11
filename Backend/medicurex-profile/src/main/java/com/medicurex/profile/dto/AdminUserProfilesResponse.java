package com.medicurex.profile.dto;

public class AdminUserProfilesResponse {

    private PatientProfileResponse patient;
    private DoctorProfileResponse doctor;

    public AdminUserProfilesResponse(PatientProfileResponse patient, DoctorProfileResponse doctor) {
        this.patient = patient;
        this.doctor = doctor;
    }

    public PatientProfileResponse getPatient() {
        return patient;
    }

    public DoctorProfileResponse getDoctor() {
        return doctor;
    }
}
