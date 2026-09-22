package com.apexcare.pharmacy.repository;

import com.apexcare.pharmacy.entity.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    List<Medicine> findByActiveTrueOrderByNameAsc();

    Optional<Medicine> findByIdAndActiveTrue(Long id);

    boolean existsByNameIgnoreCaseAndActiveTrue(String name);

    boolean existsByNameIgnoreCaseAndActiveTrueAndIdNot(String name, Long id);

    @Query("""
            select m from Medicine m
            where m.active = true
              and (
                lower(m.name) like lower(concat('%', :query, '%'))
                or lower(coalesce(m.description, '')) like lower(concat('%', :query, '%'))
                or lower(coalesce(m.category, '')) like lower(concat('%', :query, '%'))
              )
            order by m.name asc
            """)
    List<Medicine> searchActive(@Param("query") String query);
}
