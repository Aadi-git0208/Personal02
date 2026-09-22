package com.apexcare.appointment.client;

public interface ProfileClient {

    DoctorProfileSnapshot getDoctor(Long doctorProfileId, String authorizationHeader);
}
