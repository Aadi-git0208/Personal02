package com.medicurex.pharmacy.repository;

import com.medicurex.pharmacy.model.AvailabilityStatus;
import com.medicurex.pharmacy.model.DosageForm;
import com.medicurex.pharmacy.model.Medicine;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    boolean existsByNormalizedNameAndStrengthIgnoreCaseAndDosageForm(
            String normalizedName,
            String strength,
            DosageForm dosageForm
    );

    boolean existsByNormalizedNameAndStrengthIgnoreCaseAndDosageFormAndIdNot(
            String normalizedName,
            String strength,
            DosageForm dosageForm,
            Long id
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Medicine m where m.id = :id")
    Optional<Medicine> lockById(@Param("id") Long id);

    Page<Medicine> findByStatusIn(List<AvailabilityStatus> statuses, Pageable pageable);

    Page<Medicine> findByCategoryIgnoreCaseAndStatusIn(
            String category,
            List<AvailabilityStatus> statuses,
            Pageable pageable
    );

    @Query("""
            select m from Medicine m
            where m.status in :statuses
              and (:category is null or lower(m.category) = lower(:category))
              and (
                    :query is null
                    or lower(m.name) like lower(concat('%', :query, '%'))
                    or lower(m.genericName) like lower(concat('%', :query, '%'))
                    or lower(m.manufacturer) like lower(concat('%', :query, '%'))
                    or m.normalizedName like lower(concat('%', :query, '%'))
                  )
            """)
    Page<Medicine> searchCatalog(
            @Param("query") String query,
            @Param("category") String category,
            @Param("statuses") List<AvailabilityStatus> statuses,
            Pageable pageable
    );

    @Query("""
            select distinct m.category from Medicine m
            where m.status in :statuses
            order by m.category
            """)
    List<String> findDistinctCategories(@Param("statuses") List<AvailabilityStatus> statuses);
}
