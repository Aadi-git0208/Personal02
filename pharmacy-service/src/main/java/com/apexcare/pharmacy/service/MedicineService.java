package com.apexcare.pharmacy.service;

import com.apexcare.pharmacy.dto.MedicineRequest;
import com.apexcare.pharmacy.dto.MedicineResponse;
import com.apexcare.pharmacy.dto.UpdateStockRequest;
import com.apexcare.pharmacy.entity.Medicine;
import com.apexcare.pharmacy.exception.ApiException;
import com.apexcare.pharmacy.repository.MedicineRepository;
import com.apexcare.pharmacy.security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class MedicineService {

    private final MedicineRepository medicineRepository;

    public MedicineService(MedicineRepository medicineRepository) {
        this.medicineRepository = medicineRepository;
    }

    @Transactional(readOnly = true)
    public List<MedicineResponse> listActive() {
        return medicineRepository.findByActiveTrueOrderByNameAsc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<MedicineResponse> search(String query) {
        if (!StringUtils.hasText(query)) {
            return listActive();
        }
        return medicineRepository.searchActive(query.trim()).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public MedicineResponse getActive(Long id) {
        Medicine medicine = medicineRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Medicine not found"));
        return toResponse(medicine);
    }

    @Transactional
    public MedicineResponse create(MedicineRequest request) {
        String name = request.getName().trim();
        if (medicineRepository.existsByNameIgnoreCaseAndActiveTrue(name)) {
            throw new ApiException(HttpStatus.CONFLICT, "An active medicine with this name already exists");
        }
        Medicine medicine = new Medicine();
        apply(medicine, request, name);
        if (request.getActive() != null) {
            medicine.setActive(request.getActive());
        }
        return toResponse(medicineRepository.save(medicine));
    }

    @Transactional
    public MedicineResponse update(Long id, MedicineRequest request) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Medicine not found"));
        String name = request.getName().trim();
        if (medicineRepository.existsByNameIgnoreCaseAndActiveTrueAndIdNot(name, id)) {
            throw new ApiException(HttpStatus.CONFLICT, "An active medicine with this name already exists");
        }
        apply(medicine, request, name);
        if (request.getActive() != null) {
            medicine.setActive(request.getActive());
        }
        return toResponse(medicineRepository.save(medicine));
    }

    @Transactional
    public void softDelete(Long id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Medicine not found"));
        medicine.setActive(false);
        medicineRepository.save(medicine);
    }

    @Transactional
    public MedicineResponse updateStock(Long id, UpdateStockRequest request, AuthenticatedUser user) {
        boolean hasAbsolute = request.getStockQuantity() != null;
        boolean hasAdjustment = request.getAdjustment() != null;
        if (hasAbsolute == hasAdjustment) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Provide either stockQuantity or adjustment");
        }
        if (hasAbsolute && (user == null || !"ADMIN".equals(user.getRole()))) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only admin can set absolute stock");
        }

        Medicine medicine = medicineRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Medicine not found"));

        int nextStock = hasAbsolute
                ? request.getStockQuantity()
                : medicine.getStockQuantity() + request.getAdjustment();
        if (nextStock < 0) {
            throw new ApiException(HttpStatus.CONFLICT, "Insufficient stock");
        }
        medicine.setStockQuantity(nextStock);
        return toResponse(medicineRepository.save(medicine));
    }

    private void apply(Medicine medicine, MedicineRequest request, String name) {
        medicine.setName(name);
        medicine.setDescription(trimToNull(request.getDescription()));
        medicine.setCategory(trimToNull(request.getCategory()));
        medicine.setPrice(request.getPrice());
        medicine.setStockQuantity(request.getStockQuantity());
        medicine.setImageUrl(trimToNull(request.getImageUrl()));
    }

    private MedicineResponse toResponse(Medicine medicine) {
        MedicineResponse response = new MedicineResponse();
        response.setId(medicine.getId());
        response.setName(medicine.getName());
        response.setDescription(medicine.getDescription());
        response.setCategory(medicine.getCategory());
        response.setPrice(medicine.getPrice());
        response.setStockQuantity(medicine.getStockQuantity());
        response.setImageUrl(medicine.getImageUrl());
        response.setActive(medicine.isActive());
        response.setCreatedAt(medicine.getCreatedAt());
        response.setUpdatedAt(medicine.getUpdatedAt());
        return response;
    }

    private static String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
