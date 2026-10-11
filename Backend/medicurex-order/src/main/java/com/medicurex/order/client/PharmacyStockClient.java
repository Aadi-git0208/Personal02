package com.medicurex.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "pharmacy-service", contextId = "pharmacyStockClient")
public interface PharmacyStockClient {

    @PostMapping("/internal/medicines/{medicineId}/stock/adjust")
    MedicineSnapshot adjustStock(
            @PathVariable("medicineId") Long medicineId,
            @RequestBody StockAdjustRequest request
    );

    @PostMapping("/internal/medicines/{medicineId}/stock/release")
    MedicineSnapshot releaseStock(
            @PathVariable("medicineId") Long medicineId,
            @RequestBody StockReleaseRequest request
    );
}
