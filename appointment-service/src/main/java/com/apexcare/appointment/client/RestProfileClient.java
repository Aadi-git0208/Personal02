package com.apexcare.appointment.client;

import com.apexcare.appointment.exception.ApiException;
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
public class RestProfileClient implements ProfileClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public RestProfileClient(
            RestTemplate restTemplate,
            @Value("${app.profile-service.base-url}") String baseUrl
    ) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    @Override
    public DoctorProfileSnapshot getDoctor(Long doctorProfileId, String authorizationHeader) {
        HttpHeaders headers = new HttpHeaders();
        if (authorizationHeader != null && !authorizationHeader.isBlank()) {
            headers.set(HttpHeaders.AUTHORIZATION, authorizationHeader);
        }

        try {
            ResponseEntity<DoctorProfileSnapshot> response = restTemplate.exchange(
                    baseUrl + "/api/profiles/doctors/{id}",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    DoctorProfileSnapshot.class,
                    doctorProfileId
            );
            DoctorProfileSnapshot body = response.getBody();
            if (body == null || body.getUserId() == null) {
                throw new ApiException(HttpStatus.BAD_GATEWAY, "Unable to load doctor profile");
            }
            return body;
        } catch (HttpClientErrorException.NotFound exception) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Doctor profile not found");
        } catch (HttpClientErrorException.Forbidden exception) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Not allowed to view this doctor profile");
        } catch (RestClientException exception) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Unable to load doctor profile");
        }
    }
}
