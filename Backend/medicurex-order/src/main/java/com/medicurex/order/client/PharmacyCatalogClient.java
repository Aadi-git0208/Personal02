package com.medicurex.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "pharmacy-service", contextId = "pharmacyCatalogClient")
public interface PharmacyCatalogClient {

    @GetMapping("/api/medicines/{medicineId}")
    MedicineSnapshot getMedicine(@PathVariable("medicineId") Long medicineId);
}
