package com.apexcare.profile.repository;

import com.apexcare.profile.entity.DoctorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, Long> {

    Optional<DoctorProfile> findByUserId(Long userId);

    @Query("select distinct p from DoctorProfile p left join fetch p.availabilities where p.userId = :userId")
    Optional<DoctorProfile> findGraphByUserId(@Param("userId") Long userId);

    @Query("select distinct p from DoctorProfile p left join fetch p.availabilities where p.id = :id")
    Optional<DoctorProfile> findGraphById(@Param("id") Long id);

    @Query("select distinct p from DoctorProfile p left join fetch p.availabilities where p.profileCompleted = true")
    List<DoctorProfile> findByProfileCompletedTrue();

    @Query("""
            select distinct p from DoctorProfile p
            left join fetch p.availabilities
            where p.profileCompleted = true
              and lower(p.specialization) like lower(concat('%', :specialization, '%'))
            """)
    List<DoctorProfile> findByProfileCompletedTrueAndSpecializationContainingIgnoreCase(
            @Param("specialization") String specialization
    );
}
