package com.medicurex.pharmacy.controller;

import com.medicurex.pharmacy.dto.CategoryListResponse;
import com.medicurex.pharmacy.dto.MedicineRequest;
import com.medicurex.pharmacy.dto.MedicineResponse;
import com.medicurex.pharmacy.dto.StatusChangeRequest;
import com.medicurex.pharmacy.dto.StockUpdateRequest;
import com.medicurex.pharmacy.dto.UpdateMedicineRequest;
import com.medicurex.pharmacy.model.AvailabilityStatus;
import com.medicurex.pharmacy.security.SecurityUtils;
import com.medicurex.pharmacy.service.MedicineService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/medicines")
public class MedicineController {

    private final MedicineService medicineService;

    public MedicineController(MedicineService medicineService) {
        this.medicineService = medicineService;
    }

    @GetMapping
    public Page<MedicineResponse> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) AvailabilityStatus status,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        return medicineService.list(q, category, status, SecurityUtils.isAdmin(), pageable);
    }

    @GetMapping("/search")
    public Page<MedicineResponse> search(
            @RequestParam("q") String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) AvailabilityStatus status,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        return medicineService.list(query, category, status, SecurityUtils.isAdmin(), pageable);
    }

    @GetMapping("/categories")
    public CategoryListResponse categories() {
        return new CategoryListResponse(medicineService.categories(SecurityUtils.isAdmin()));
    }

    @GetMapping("/{medicineId}")
    public MedicineResponse get(@PathVariable Long medicineId) {
        return medicineService.get(medicineId, SecurityUtils.isAdmin());
    }

    @PostMapping
    public ResponseEntity<MedicineResponse> create(@Valid @RequestBody MedicineRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(medicineService.create(request));
    }

    @PutMapping("/{medicineId}")
    public MedicineResponse update(
            @PathVariable Long medicineId,
            @Valid @RequestBody UpdateMedicineRequest request
    ) {
        return medicineService.update(medicineId, request);
    }

    @PatchMapping("/{medicineId}/status")
    public MedicineResponse updateStatus(
            @PathVariable Long medicineId,
            @Valid @RequestBody StatusChangeRequest request
    ) {
        return medicineService.updateStatus(medicineId, request);
    }

    @PatchMapping("/{medicineId}/stock")
    public MedicineResponse updateStock(
            @PathVariable Long medicineId,
            @Valid @RequestBody StockUpdateRequest request
    ) {
        return medicineService.updateStock(medicineId, request);
    }
}
