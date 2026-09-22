package com.apexcare.chat.repository;

import com.apexcare.chat.entity.Conversation;
import com.apexcare.chat.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByConversationOrderBySentAtAsc(Conversation conversation);
}
