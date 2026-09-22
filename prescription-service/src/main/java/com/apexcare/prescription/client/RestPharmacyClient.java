package com.apexcare.prescription.client;

import com.apexcare.prescription.exception.ApiException;
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
public class RestPharmacyClient implements PharmacyClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public RestPharmacyClient(
            RestTemplate restTemplate,
            @Value("${app.pharmacy-service.base-url}") String baseUrl
    ) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    @Override
    public MedicineSnapshot getMedicine(Long medicineId, String authorizationHeader) {
        HttpHeaders headers = new HttpHeaders();
        if (authorizationHeader != null && !authorizationHeader.isBlank()) {
            headers.set(HttpHeaders.AUTHORIZATION, authorizationHeader);
        }

        try {
            ResponseEntity<MedicineSnapshot> response = restTemplate.exchange(
                    baseUrl + "/api/medicines/{id}",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    MedicineSnapshot.class,
                    medicineId
            );
            MedicineSnapshot body = response.getBody();
            if (body == null || body.getId() == null) {
                throw new ApiException(HttpStatus.BAD_GATEWAY, "Unable to load medicine");
            }
            return body;
        } catch (HttpClientErrorException.NotFound exception) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Medicine not found");
        } catch (HttpClientErrorException.Forbidden exception) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Not allowed to view this medicine");
        } catch (RestClientException exception) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Unable to load medicine");
        }
    }
}
