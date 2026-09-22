package com.apexcare.chat.repository;

import com.apexcare.chat.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByAppointmentId(Long appointmentId);

    List<Conversation> findByPatientIdOrderByUpdatedAtDesc(Long patientId);

    List<Conversation> findByDoctorIdOrderByUpdatedAtDesc(Long doctorId);

    List<Conversation> findAllByOrderByUpdatedAtDesc();
}
