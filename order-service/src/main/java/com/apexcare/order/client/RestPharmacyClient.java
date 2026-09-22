package com.apexcare.order.client;

import com.apexcare.order.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

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
        try {
            ResponseEntity<MedicineSnapshot> response = restTemplate.exchange(
                    baseUrl + "/api/medicines/{id}",
                    HttpMethod.GET,
                    new HttpEntity<>(headers(authorizationHeader)),
                    MedicineSnapshot.class,
                    medicineId
            );
            MedicineSnapshot body = response.getBody();
            if (body == null || body.getId() == null || body.getPrice() == null || body.getStockQuantity() == null) {
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

    @Override
    public MedicineSnapshot adjustStock(Long medicineId, int adjustment, String authorizationHeader) {
        try {
            ResponseEntity<MedicineSnapshot> response = restTemplate.exchange(
                    baseUrl + "/api/medicines/{id}/stock",
                    HttpMethod.PATCH,
                    new HttpEntity<>(Map.of("adjustment", adjustment), headers(authorizationHeader)),
                    MedicineSnapshot.class,
                    medicineId
            );
            MedicineSnapshot body = response.getBody();
            if (body == null || body.getStockQuantity() == null) {
                throw new ApiException(HttpStatus.BAD_GATEWAY, "Unable to update medicine stock");
            }
            return body;
        } catch (HttpClientErrorException.NotFound exception) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Medicine not found");
        } catch (HttpClientErrorException.Conflict exception) {
            throw new ApiException(HttpStatus.CONFLICT, "Insufficient stock");
        } catch (HttpClientErrorException.Forbidden exception) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Not allowed to update medicine stock");
        } catch (RestClientException exception) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Unable to update medicine stock");
        }
    }

    private HttpHeaders headers(String authorizationHeader) {
        HttpHeaders headers = new HttpHeaders();
        if (authorizationHeader != null && !authorizationHeader.isBlank()) {
            headers.set(HttpHeaders.AUTHORIZATION, authorizationHeader);
        }
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
