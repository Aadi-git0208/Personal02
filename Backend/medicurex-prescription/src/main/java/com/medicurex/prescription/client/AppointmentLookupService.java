package com.medicurex.prescription.client;

import com.medicurex.prescription.exception.ApiException;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Set;

@Service
public class AppointmentLookupService {

    /**
     * Prescriptions are allowed for CONFIRMED visits (in progress) and COMPLETED visits.
     * PENDING, CANCELLED, and REJECTED are rejected. These values match AppointmentStatus exactly.
     */
    private static final Set<String> ELIGIBLE_STATUSES = Set.of("CONFIRMED", "COMPLETED");

    private final AppointmentClient appointmentClient;

    public AppointmentLookupService(AppointmentClient appointmentClient) {
        this.appointmentClient = appointmentClient;
    }

    public AppointmentSnapshot requireEligibleAppointment(Long appointmentId, Long doctorUserId) {
        AppointmentSnapshot appointment = fetch(appointmentId);
        if (appointment.getPatientUserId() == null || appointment.getDoctorUserId() == null) {
            throw new ApiException(
                    HttpStatus.BAD_GATEWAY,
                    "Appointment Service returned an incomplete appointment"
            );
        }
        if (!doctorUserId.equals(appointment.getDoctorUserId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You are not the doctor assigned to this appointment");
        }

        String status = appointment.getStatus() == null ? "" : appointment.getStatus().trim().toUpperCase(Locale.ROOT);
        if (!ELIGIBLE_STATUSES.contains(status)) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Prescriptions can only be written for confirmed or completed appointments"
            );
        }
        return appointment;
    }

    private AppointmentSnapshot fetch(Long appointmentId) {
        try {
            AppointmentSnapshot appointment = appointmentClient.getAppointment(appointmentId);
            if (appointment == null || appointment.getId() == null) {
                throw new ApiException(
                        HttpStatus.BAD_GATEWAY,
                        "Appointment Service returned an incomplete appointment"
                );
            }
            return appointment;
        } catch (ApiException exception) {
            throw exception;
        } catch (FeignException.NotFound exception) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Appointment not found");
        } catch (FeignException.Forbidden exception) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You are not the doctor assigned to this appointment");
        } catch (FeignException exception) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Appointment Service is unavailable");
        } catch (RuntimeException exception) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Appointment Service is unavailable");
        }
    }
}
