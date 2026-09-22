package com.apexcare.prescription.repository;

import com.apexcare.prescription.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    boolean existsByAppointmentId(Long appointmentId);

    @Query("select distinct p from Prescription p left join fetch p.items where p.id = :id")
    Optional<Prescription> findGraphById(@Param("id") Long id);

    @Query("select distinct p from Prescription p left join fetch p.items where p.doctorId = :doctorId order by p.createdAt desc")
    List<Prescription> findGraphByDoctorId(@Param("doctorId") Long doctorId);

    @Query("select distinct p from Prescription p left join fetch p.items where p.patientId = :patientId order by p.createdAt desc")
    List<Prescription> findGraphByPatientId(@Param("patientId") Long patientId);

    @Query("select distinct p from Prescription p left join fetch p.items order by p.createdAt desc")
    List<Prescription> findAllGraph();
}
