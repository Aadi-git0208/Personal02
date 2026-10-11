package com.medicurex.order.client;

import com.medicurex.order.exception.ApiException;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Locale;

@Service
public class CatalogLookupService {

    private final PharmacyCatalogClient pharmacyCatalogClient;

    public CatalogLookupService(PharmacyCatalogClient pharmacyCatalogClient) {
        this.pharmacyCatalogClient = pharmacyCatalogClient;
    }

    public MedicineSnapshot requireOrderable(Long medicineId) {
        MedicineSnapshot medicine = fetch(medicineId);
        if (medicine.getId() == null || medicine.getName() == null || medicine.getUnitPrice() == null) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Pharmacy Catalog returned an incomplete medicine");
        }
        if (medicine.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Pharmacy Catalog returned an invalid price");
        }

        String status = medicine.getStatus() == null ? "" : medicine.getStatus().trim().toUpperCase(Locale.ROOT);
        if (!"ACTIVE".equals(status)) {
            throw new ApiException(HttpStatus.CONFLICT, "Medicine is not available for order");
        }
        if (Boolean.TRUE.equals(medicine.getPrescriptionRequired())) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Prescription-required medicines cannot be ordered until catalog items can be verified against a patient prescription"
            );
        }
        return medicine;
    }

    private MedicineSnapshot fetch(Long medicineId) {
        try {
            MedicineSnapshot medicine = pharmacyCatalogClient.getMedicine(medicineId);
            if (medicine == null) {
                throw new ApiException(HttpStatus.BAD_GATEWAY, "Pharmacy Catalog returned an incomplete medicine");
            }
            return medicine;
        } catch (ApiException exception) {
            throw exception;
        } catch (FeignException.NotFound exception) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Medicine not found");
        } catch (FeignException exception) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Pharmacy Catalog is unavailable");
        } catch (RuntimeException exception) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Pharmacy Catalog is unavailable");
        }
    }
}
