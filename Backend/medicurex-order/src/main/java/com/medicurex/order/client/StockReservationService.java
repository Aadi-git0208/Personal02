package com.medicurex.order.client;

import com.medicurex.order.exception.ApiException;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class StockReservationService {

    private static final Logger log = LoggerFactory.getLogger(StockReservationService.class);

    private final PharmacyStockClient pharmacyStockClient;

    public StockReservationService(PharmacyStockClient pharmacyStockClient) {
        this.pharmacyStockClient = pharmacyStockClient;
    }

    public void reserve(Long medicineId, int quantity, String operationKey) {
        try {
            pharmacyStockClient.adjustStock(medicineId, new StockAdjustRequest(-quantity, operationKey));
        } catch (ApiException exception) {
            throw exception;
        } catch (FeignException.Conflict exception) {
            throw new ApiException(HttpStatus.CONFLICT, "Insufficient stock");
        } catch (FeignException.NotFound exception) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Medicine not found");
        } catch (FeignException exception) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Pharmacy Catalog is unavailable");
        } catch (RuntimeException exception) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Pharmacy Catalog is unavailable");
        }
    }

    public void release(Long medicineId, String operationKey) {
        try {
            pharmacyStockClient.releaseStock(medicineId, new StockReleaseRequest(operationKey));
        } catch (RuntimeException exception) {
            log.error("Failed to release stock for medicine {} key {}", medicineId, operationKey, exception);
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Unable to release reserved stock");
        }
    }

    public void releaseQuietly(Long medicineId, String operationKey) {
        try {
            pharmacyStockClient.releaseStock(medicineId, new StockReleaseRequest(operationKey));
        } catch (RuntimeException exception) {
            log.error("Compensation release failed for medicine {} key {}", medicineId, operationKey, exception);
        }
    }
}
