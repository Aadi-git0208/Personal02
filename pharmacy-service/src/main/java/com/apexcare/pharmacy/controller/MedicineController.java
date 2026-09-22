package com.apexcare.pharmacy.controller;

import com.apexcare.pharmacy.dto.MedicineRequest;
import com.apexcare.pharmacy.dto.MedicineResponse;
import com.apexcare.pharmacy.dto.UpdateStockRequest;
import com.apexcare.pharmacy.security.AuthenticatedUser;
import com.apexcare.pharmacy.service.MedicineService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/medicines")
public class MedicineController {

    private final MedicineService medicineService;

    public MedicineController(MedicineService medicineService) {
        this.medicineService = medicineService;
    }

    @GetMapping
    public ResponseEntity<List<MedicineResponse>> list() {
        return ResponseEntity.ok(medicineService.listActive());
    }

    @GetMapping("/search")
    public ResponseEntity<List<MedicineResponse>> search(@RequestParam(required = false) String query) {
        return ResponseEntity.ok(medicineService.search(query));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicineResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(medicineService.getActive(id));
    }

    @PostMapping
    public ResponseEntity<MedicineResponse> create(@Valid @RequestBody MedicineRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(medicineService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicineResponse> update(@PathVariable Long id, @Valid @RequestBody MedicineRequest request) {
        return ResponseEntity.ok(medicineService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        medicineService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<MedicineResponse> updateStock(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody UpdateStockRequest request
    ) {
        return ResponseEntity.ok(
                medicineService.updateStock(id, request, AuthenticatedUser.from(authentication))
        );
    }
}
