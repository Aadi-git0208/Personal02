package com.apexcare.chat.client;

import com.apexcare.chat.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class RestAppointmentClient implements AppointmentClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public RestAppointmentClient(
            RestTemplate restTemplate,
            @Value("${app.appointment-service.base-url}") String baseUrl
    ) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    @Override
    public AppointmentSnapshot getAppointment(Long appointmentId, String authorizationHeader) {
        HttpHeaders headers = new HttpHeaders();
        if (authorizationHeader != null && !authorizationHeader.isBlank()) {
            headers.set(HttpHeaders.AUTHORIZATION, authorizationHeader);
        }

        try {
            ResponseEntity<AppointmentSnapshot> response = restTemplate.exchange(
                    baseUrl + "/api/appointments/{id}",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    AppointmentSnapshot.class,
                    appointmentId
            );
            AppointmentSnapshot body = response.getBody();
            if (body == null || body.getPatientId() == null || body.getDoctorId() == null) {
                throw new ApiException(HttpStatus.BAD_GATEWAY, "Unable to load appointment");
            }
            return body;
        } catch (HttpClientErrorException.NotFound exception) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Appointment not found");
        } catch (HttpClientErrorException.Forbidden exception) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Not allowed to use this appointment");
        } catch (RestClientException exception) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Unable to load appointment");
        }
    }
}
