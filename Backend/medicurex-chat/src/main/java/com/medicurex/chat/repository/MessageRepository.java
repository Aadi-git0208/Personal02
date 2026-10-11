package com.medicurex.chat.repository;

import com.medicurex.chat.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    Page<Message> findByConversationIdOrderByCreatedAtAsc(Long conversationId, Pageable pageable);

    @Query("""
            select m from Message m
            where m.conversation.id = :conversationId
              and m.senderUserId <> :readerUserId
              and m.readAt is null
            """)
    List<Message> findUnreadFromOtherParty(
            @Param("conversationId") Long conversationId,
            @Param("readerUserId") Long readerUserId
    );

    @Modifying
    @Query("""
            update Message m
            set m.readAt = :readAt
            where m.conversation.id = :conversationId
              and m.senderUserId <> :readerUserId
              and m.readAt is null
            """)
    int markOtherPartyMessagesRead(
            @Param("conversationId") Long conversationId,
            @Param("readerUserId") Long readerUserId,
            @Param("readAt") Instant readAt
    );
}
