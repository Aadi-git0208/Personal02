package com.medicurex.chat.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "appointment-service", contextId = "appointmentClient")
public interface AppointmentClient {

    @GetMapping("/api/appointments/{appointmentId}")
    AppointmentSnapshot getAppointment(@PathVariable("appointmentId") Long appointmentId);
}
