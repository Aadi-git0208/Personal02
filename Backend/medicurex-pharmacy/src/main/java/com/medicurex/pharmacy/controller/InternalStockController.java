package com.medicurex.pharmacy.controller;

import com.medicurex.pharmacy.dto.MedicineResponse;
import com.medicurex.pharmacy.dto.StockAdjustRequest;
import com.medicurex.pharmacy.dto.StockReleaseRequest;
import com.medicurex.pharmacy.service.MedicineService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/medicines")
public class InternalStockController {

    private final MedicineService medicineService;

    public InternalStockController(MedicineService medicineService) {
        this.medicineService = medicineService;
    }

    @PostMapping("/{medicineId}/stock/adjust")
    public MedicineResponse adjust(
            @PathVariable Long medicineId,
            @Valid @RequestBody StockAdjustRequest request
    ) {
        return medicineService.adjustStock(medicineId, request);
    }

    @PostMapping("/{medicineId}/stock/release")
    public MedicineResponse release(
            @PathVariable Long medicineId,
            @Valid @RequestBody StockReleaseRequest request
    ) {
        return medicineService.releaseStock(medicineId, request);
    }
}
