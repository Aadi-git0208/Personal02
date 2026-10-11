package com.medicurex.prescription.repository;

import com.medicurex.prescription.model.Prescription;
import com.medicurex.prescription.model.PrescriptionStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    @EntityGraph(attributePaths = "items")
    Optional<Prescription> findWithItemsById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Prescription p where p.appointmentId = :appointmentId and p.status = :status")
    Optional<Prescription> lockActiveByAppointmentId(
            @Param("appointmentId") Long appointmentId,
            @Param("status") PrescriptionStatus status
    );

    boolean existsByAppointmentIdAndStatus(Long appointmentId, PrescriptionStatus status);

    @EntityGraph(attributePaths = "items")
    Page<Prescription> findByDoctorUserIdOrderByCreatedAtDesc(Long doctorUserId, Pageable pageable);

    @EntityGraph(attributePaths = "items")
    Page<Prescription> findByPatientUserIdOrderByCreatedAtDesc(Long patientUserId, Pageable pageable);
}
