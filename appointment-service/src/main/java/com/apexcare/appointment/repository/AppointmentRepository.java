package com.apexcare.appointment.repository;

import com.apexcare.appointment.entity.Appointment;
import com.apexcare.appointment.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientIdOrderByAppointmentDateDescStartTimeDesc(Long patientId);

    List<Appointment> findByDoctorIdOrderByAppointmentDateDescStartTimeDesc(Long doctorId);

    List<Appointment> findAllByOrderByAppointmentDateDescStartTimeDesc();

    @Query("""
            select a from Appointment a
            where a.doctorId = :doctorId
              and a.appointmentDate = :appointmentDate
              and a.status in :statuses
              and a.startTime < :endTime
              and a.endTime > :startTime
            """)
    List<Appointment> findOverlapping(
            @Param("doctorId") Long doctorId,
            @Param("appointmentDate") LocalDate appointmentDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("statuses") Collection<AppointmentStatus> statuses
    );
}
