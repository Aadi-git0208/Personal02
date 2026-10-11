package com.medicurex.order.repository;

import com.medicurex.order.model.PharmacyOrder;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<PharmacyOrder, Long> {

    @EntityGraph(attributePaths = "items")
    Optional<PharmacyOrder> findWithItemsById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from PharmacyOrder o where o.id = :id")
    Optional<PharmacyOrder> lockById(@Param("id") Long id);

    Optional<PharmacyOrder> findByPatientUserIdAndIdempotencyKey(Long patientUserId, String idempotencyKey);

    @EntityGraph(attributePaths = "items")
    Page<PharmacyOrder> findByPatientUserIdOrderByCreatedAtDesc(Long patientUserId, Pageable pageable);

    @EntityGraph(attributePaths = "items")
    Page<PharmacyOrder> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
