package com.apexcare.profile.repository;

import com.apexcare.profile.entity.DoctorAvailability;
import com.apexcare.profile.entity.DoctorProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailability, Long> {

    List<DoctorAvailability> findByDoctorProfileOrderByDayOfWeekAscStartTimeAsc(DoctorProfile doctorProfile);

    Optional<DoctorAvailability> findByIdAndDoctorProfile(Long id, DoctorProfile doctorProfile);

    List<DoctorAvailability> findByDoctorProfileAndDayOfWeek(DoctorProfile doctorProfile, DayOfWeek dayOfWeek);
}
