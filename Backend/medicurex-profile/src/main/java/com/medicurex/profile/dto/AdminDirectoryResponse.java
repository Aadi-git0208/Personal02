package com.medicurex.profile.dto;

import java.util.List;

public class AdminDirectoryResponse {

    private List<PatientProfileResponse> patients;
    private List<DoctorProfileResponse> doctors;

    public AdminDirectoryResponse(List<PatientProfileResponse> patients, List<DoctorProfileResponse> doctors) {
        this.patients = patients;
        this.doctors = doctors;
    }

    public List<PatientProfileResponse> getPatients() {
        return patients;
    }

    public List<DoctorProfileResponse> getDoctors() {
        return doctors;
    }
}
