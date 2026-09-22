package com.apexcare.order.repository;

import com.apexcare.order.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    @Query("select distinct c from Cart c left join fetch c.items where c.patientId = :patientId")
    Optional<Cart> findGraphByPatientId(@Param("patientId") Long patientId);
}
