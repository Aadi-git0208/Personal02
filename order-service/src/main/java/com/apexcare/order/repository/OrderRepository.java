package com.apexcare.order.repository;

import com.apexcare.order.entity.PharmacyOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<PharmacyOrder, Long> {

    @Query("select distinct o from PharmacyOrder o left join fetch o.items where o.id = :id")
    Optional<PharmacyOrder> findGraphById(@Param("id") Long id);

    @Query("select distinct o from PharmacyOrder o left join fetch o.items where o.patientId = :patientId order by o.createdAt desc")
    List<PharmacyOrder> findGraphByPatientId(@Param("patientId") Long patientId);

    @Query("select distinct o from PharmacyOrder o left join fetch o.items order by o.createdAt desc")
    List<PharmacyOrder> findAllGraph();
}
