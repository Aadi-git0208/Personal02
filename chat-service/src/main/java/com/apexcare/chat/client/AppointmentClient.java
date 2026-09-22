package com.apexcare.chat.client;

public interface AppointmentClient {

    AppointmentSnapshot getAppointment(Long appointmentId, String authorizationHeader);
}
