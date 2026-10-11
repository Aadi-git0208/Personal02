package com.medicurex.profile.repository;

import com.medicurex.profile.model.PatientProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientProfileRepository extends JpaRepository<PatientProfile, Long> {

    Optional<PatientProfile> findByAuthUserId(Long authUserId);

    boolean existsByAuthUserId(Long authUserId);
}
