package com.medicurex.chat.repository;

import com.medicurex.chat.model.Conversation;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByConversationKey(String conversationKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Conversation c where c.conversationKey = :key")
    Optional<Conversation> lockByConversationKey(@Param("key") String conversationKey);

    Page<Conversation> findByPatientUserIdOrDoctorUserIdOrderByLastMessageAtDescUpdatedAtDesc(
            Long patientUserId,
            Long doctorUserId,
            Pageable pageable
    );
}
