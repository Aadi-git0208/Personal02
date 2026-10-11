package com.medicurex.appointment.repository;

import com.medicurex.appointment.model.Appointment;
import com.medicurex.appointment.model.AppointmentStatus;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    Page<Appointment> findByPatientUserIdOrderByAppointmentDateAscStartTimeAsc(Long patientUserId, Pageable pageable);

    Page<Appointment> findByDoctorUserIdOrderByAppointmentDateAscStartTimeAsc(Long doctorUserId, Pageable pageable);

    Page<Appointment> findAllByOrderByAppointmentDateAscStartTimeAsc(Pageable pageable);

    List<Appointment> findByDoctorUserIdAndAppointmentDateAndStatusIn(
            Long doctorUserId,
            LocalDate appointmentDate,
            Collection<AppointmentStatus> statuses
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000"))
    @Query("""
            select a from Appointment a
            where a.doctorUserId = :doctorUserId
              and a.appointmentDate = :date
              and a.status in :active
              and a.startTime < :endTime
              and a.endTime > :startTime
            """)
    List<Appointment> lockDoctorOverlaps(
            @Param("doctorUserId") Long doctorUserId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("active") Collection<AppointmentStatus> active
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000"))
    @Query("""
            select a from Appointment a
            where a.patientUserId = :patientUserId
              and a.appointmentDate = :date
              and a.status in :active
              and a.startTime < :endTime
              and a.endTime > :startTime
            """)
    List<Appointment> lockPatientOverlaps(
            @Param("patientUserId") Long patientUserId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("active") Collection<AppointmentStatus> active
    );
}
