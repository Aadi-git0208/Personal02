package com.medicurex.pharmacy.service;

import com.medicurex.pharmacy.dto.MedicineRequest;
import com.medicurex.pharmacy.dto.MedicineResponse;
import com.medicurex.pharmacy.dto.StatusChangeRequest;
import com.medicurex.pharmacy.dto.StockAdjustRequest;
import com.medicurex.pharmacy.dto.StockReleaseRequest;
import com.medicurex.pharmacy.dto.StockUpdateRequest;
import com.medicurex.pharmacy.dto.UpdateMedicineRequest;
import com.medicurex.pharmacy.exception.ApiException;
import com.medicurex.pharmacy.model.AvailabilityStatus;
import com.medicurex.pharmacy.model.Medicine;
import com.medicurex.pharmacy.model.StockAdjustment;
import com.medicurex.pharmacy.repository.MedicineRepository;
import com.medicurex.pharmacy.repository.StockAdjustmentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MedicineService {

    private static final List<AvailabilityStatus> PUBLIC_STATUSES =
            List.of(AvailabilityStatus.ACTIVE, AvailabilityStatus.OUT_OF_STOCK);
    private static final List<AvailabilityStatus> ALL_STATUSES = List.of(AvailabilityStatus.values());

    private final MedicineRepository medicineRepository;
    private final StockAdjustmentRepository stockAdjustmentRepository;

    public MedicineService(
            MedicineRepository medicineRepository,
            StockAdjustmentRepository stockAdjustmentRepository
    ) {
        this.medicineRepository = medicineRepository;
        this.stockAdjustmentRepository = stockAdjustmentRepository;
    }

    @Transactional(readOnly = true)
    public Page<MedicineResponse> list(String query, String category, AvailabilityStatus status, boolean admin, Pageable pageable) {
        if (!admin && status == AvailabilityStatus.INACTIVE) {
            return Page.empty(pageable);
        }
        List<AvailabilityStatus> statuses = resolveStatuses(status, admin);
        String normalizedQuery = blankToNull(query);
        String normalizedCategory = blankToNull(category);
        return medicineRepository
                .searchCatalog(normalizedQuery, normalizedCategory, statuses, pageable)
                .map(MedicineResponse::from);
    }

    @Transactional(readOnly = true)
    public MedicineResponse get(Long medicineId, boolean admin) {
        Medicine medicine = require(medicineId);
        if (!admin && !medicine.isBrowsable()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Medicine not found");
        }
        return MedicineResponse.from(medicine);
    }

    @Transactional(readOnly = true)
    public List<String> categories(boolean admin) {
        return medicineRepository.findDistinctCategories(admin ? ALL_STATUSES : PUBLIC_STATUSES);
    }

    @Transactional
    public MedicineResponse create(MedicineRequest request) {
        String normalizedName = Medicine.normalize(request.getName());
        String strength = request.getStrength().trim();
        if (medicineRepository.existsByNormalizedNameAndStrengthIgnoreCaseAndDosageForm(
                normalizedName, strength, request.getDosageForm())) {
            throw new ApiException(HttpStatus.CONFLICT, "A medicine with the same name, strength, and form already exists");
        }

        Medicine medicine = new Medicine();
        applyCatalogFields(
                medicine,
                request.getName(),
                request.getGenericName(),
                request.getManufacturer(),
                request.getDescription(),
                request.getDosageForm(),
                strength,
                request.getCategory(),
                request.getUnitPrice(),
                request.getPrescriptionRequired(),
                request.getImageUrl()
        );
        int stock = request.getStockQuantity() == null ? 0 : request.getStockQuantity();
        medicine.setStockQuantity(stock);
        medicine.setStatus(stock > 0 ? AvailabilityStatus.ACTIVE : AvailabilityStatus.OUT_OF_STOCK);
        return MedicineResponse.from(medicineRepository.save(medicine));
    }

    @Transactional
    public MedicineResponse update(Long medicineId, UpdateMedicineRequest request) {
        Medicine medicine = require(medicineId);
        String normalizedName = Medicine.normalize(request.getName());
        String strength = request.getStrength().trim();
        if (medicineRepository.existsByNormalizedNameAndStrengthIgnoreCaseAndDosageFormAndIdNot(
                normalizedName, strength, request.getDosageForm(), medicineId)) {
            throw new ApiException(HttpStatus.CONFLICT, "A medicine with the same name, strength, and form already exists");
        }

        applyCatalogFields(
                medicine,
                request.getName(),
                request.getGenericName(),
                request.getManufacturer(),
                request.getDescription(),
                request.getDosageForm(),
                strength,
                request.getCategory(),
                request.getUnitPrice(),
                request.getPrescriptionRequired(),
                request.getImageUrl()
        );
        return MedicineResponse.from(medicineRepository.save(medicine));
    }

    @Transactional
    public MedicineResponse updateStatus(Long medicineId, StatusChangeRequest request) {
        if (request.getStatus() == AvailabilityStatus.OUT_OF_STOCK) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "OUT_OF_STOCK is derived from stock quantity. Use the stock endpoint or set status to ACTIVE or INACTIVE"
            );
        }

        Medicine medicine = medicineRepository.lockById(medicineId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Medicine not found"));
        medicine.setStatus(request.getStatus());
        medicine.syncAvailabilityFromStock();
        return MedicineResponse.from(medicineRepository.save(medicine));
    }

    @Transactional
    public MedicineResponse updateStock(Long medicineId, StockUpdateRequest request) {
        Medicine medicine = medicineRepository.lockById(medicineId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Medicine not found"));
        medicine.setStockQuantity(request.getQuantity());
        medicine.syncAvailabilityFromStock();
        return MedicineResponse.from(medicineRepository.save(medicine));
    }

    @Transactional
    public MedicineResponse adjustStock(Long medicineId, StockAdjustRequest request) {
        String operationKey = request.getOperationKey().trim();
        if (stockAdjustmentRepository.existsByOperationKey(operationKey)) {
            return MedicineResponse.from(require(medicineId));
        }

        int delta = request.getDelta();
        if (delta == 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Stock delta cannot be zero");
        }

        Medicine medicine = medicineRepository.lockById(medicineId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Medicine not found"));

        if (delta < 0) {
            if (medicine.getStatus() != AvailabilityStatus.ACTIVE) {
                throw new ApiException(HttpStatus.CONFLICT, "Medicine is not available for order");
            }
            if (medicine.getStockQuantity() < -delta) {
                throw new ApiException(HttpStatus.CONFLICT, "Insufficient stock");
            }
        }

        int updated = medicine.getStockQuantity() + delta;
        if (updated < 0) {
            throw new ApiException(HttpStatus.CONFLICT, "Insufficient stock");
        }

        medicine.setStockQuantity(updated);
        medicine.syncAvailabilityFromStock();
        medicineRepository.save(medicine);

        StockAdjustment adjustment = new StockAdjustment();
        adjustment.setOperationKey(operationKey);
        adjustment.setMedicineId(medicineId);
        adjustment.setDelta(delta);
        stockAdjustmentRepository.save(adjustment);

        return MedicineResponse.from(medicine);
    }

    @Transactional
    public MedicineResponse releaseStock(Long medicineId, StockReleaseRequest request) {
        String operationKey = request.getOperationKey().trim();
        var adjustment = stockAdjustmentRepository.findByOperationKey(operationKey);
        Medicine medicine = medicineRepository.lockById(medicineId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Medicine not found"));
        if (adjustment.isEmpty()) {
            return MedicineResponse.from(medicine);
        }

        StockAdjustment applied = adjustment.get();
        if (applied.getDelta() >= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only reservation operations can be released");
        }
        if (!applied.getMedicineId().equals(medicineId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Operation key does not belong to this medicine");
        }

        medicine.setStockQuantity(medicine.getStockQuantity() - applied.getDelta());
        medicine.syncAvailabilityFromStock();
        medicineRepository.save(medicine);
        stockAdjustmentRepository.delete(applied);
        return MedicineResponse.from(medicine);
    }

    private Medicine require(Long medicineId) {
        return medicineRepository.findById(medicineId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Medicine not found"));
    }

    private List<AvailabilityStatus> resolveStatuses(AvailabilityStatus requested, boolean admin) {
        if (!admin) {
            if (requested == null) {
                return PUBLIC_STATUSES;
            }
            return List.of(requested);
        }
        if (requested == null) {
            return ALL_STATUSES;
        }
        return List.of(requested);
    }

    private void applyCatalogFields(
            Medicine medicine,
            String name,
            String genericName,
            String manufacturer,
            String description,
            com.medicurex.pharmacy.model.DosageForm dosageForm,
            String strength,
            String category,
            java.math.BigDecimal unitPrice,
            Boolean prescriptionRequired,
            String imageUrl
    ) {
        medicine.setName(name.trim());
        medicine.setGenericName(normalize(genericName));
        medicine.setManufacturer(normalize(manufacturer));
        medicine.setDescription(normalize(description));
        medicine.setDosageForm(dosageForm);
        medicine.setStrength(strength);
        medicine.setCategory(category.trim());
        medicine.setUnitPrice(unitPrice);
        medicine.setPrescriptionRequired(Boolean.TRUE.equals(prescriptionRequired));
        medicine.setImageUrl(normalize(imageUrl));
        medicine.refreshNormalizedName();
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
