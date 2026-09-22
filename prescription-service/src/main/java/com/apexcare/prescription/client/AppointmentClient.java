package com.apexcare.prescription.client;

public interface AppointmentClient {

    AppointmentSnapshot getAppointment(Long appointmentId, String authorizationHeader);
}
