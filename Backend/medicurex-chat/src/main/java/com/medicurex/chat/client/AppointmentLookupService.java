package com.medicurex.chat.client;

import com.medicurex.chat.exception.ApiException;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Set;

@Service
public class AppointmentLookupService {

    private static final Set<String> BLOCKED_STATUSES = Set.of("CANCELLED", "REJECTED");

    private final AppointmentClient appointmentClient;

    public AppointmentLookupService(AppointmentClient appointmentClient) {
        this.appointmentClient = appointmentClient;
    }

    public AppointmentSnapshot requireParticipantAppointment(Long appointmentId, Long requesterUserId) {
        AppointmentSnapshot appointment = fetch(appointmentId);
        if (appointment.getPatientUserId() == null || appointment.getDoctorUserId() == null) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Appointment Service returned an incomplete appointment");
        }
        if (!requesterUserId.equals(appointment.getPatientUserId())
                && !requesterUserId.equals(appointment.getDoctorUserId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You are not a participant of this appointment");
        }

        String status = appointment.getStatus() == null ? "" : appointment.getStatus().trim().toUpperCase(Locale.ROOT);
        if (BLOCKED_STATUSES.contains(status)) {
            throw new ApiException(HttpStatus.CONFLICT, "Cannot start a conversation for a cancelled or rejected appointment");
        }
        return appointment;
    }

    private AppointmentSnapshot fetch(Long appointmentId) {
        try {
            AppointmentSnapshot appointment = appointmentClient.getAppointment(appointmentId);
            if (appointment == null || appointment.getId() == null) {
                throw new ApiException(HttpStatus.BAD_GATEWAY, "Appointment Service returned an incomplete appointment");
            }
            return appointment;
        } catch (ApiException exception) {
            throw exception;
        } catch (FeignException.NotFound exception) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Appointment not found");
        } catch (FeignException.Forbidden exception) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You are not a participant of this appointment");
        } catch (FeignException exception) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Appointment Service is unavailable");
        } catch (RuntimeException exception) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Appointment Service is unavailable");
        }
    }
}
