package com.medicurex.profile.repository;

import com.medicurex.profile.model.ApprovalStatus;
import com.medicurex.profile.model.DoctorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, Long> {

    @Query("select distinct d from DoctorProfile d left join fetch d.availabilitySlots where d.authUserId = :authUserId")
    Optional<DoctorProfile> findByAuthUserId(@Param("authUserId") Long authUserId);

    boolean existsByAuthUserId(Long authUserId);

    @Query("select distinct d from DoctorProfile d left join fetch d.availabilitySlots where d.approvalStatus = :status order by d.fullName asc")
    List<DoctorProfile> findByApprovalStatusOrderByFullNameAsc(@Param("status") ApprovalStatus approvalStatus);

    @Query("select distinct d from DoctorProfile d left join fetch d.availabilitySlots where d.id = :id and d.approvalStatus = :status")
    Optional<DoctorProfile> findByIdAndApprovalStatus(@Param("id") Long id, @Param("status") ApprovalStatus approvalStatus);

    @Query("select distinct d from DoctorProfile d left join fetch d.availabilitySlots")
    List<DoctorProfile> findAllWithSlots();
}
