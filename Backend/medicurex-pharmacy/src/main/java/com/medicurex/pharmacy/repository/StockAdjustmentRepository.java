package com.medicurex.pharmacy.repository;

import com.medicurex.pharmacy.model.StockAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StockAdjustmentRepository extends JpaRepository<StockAdjustment, Long> {

    boolean existsByOperationKey(String operationKey);

    Optional<StockAdjustment> findByOperationKey(String operationKey);
}
