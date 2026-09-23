package com.ecommerce.project.repositories;

import com.ecommerce.project.model.Message;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findByConversationIdOrderByCreatedAtAsc(Long conversationId, Pageable pageable);

    Message findTopByConversationIdOrderByCreatedAtDesc(Long conversationId);

    int countByConversationIdAndSenderIdNotAndReadFalse(Long conversationId, Long senderId);


    @Transactional
    @Modifying
    @Query("UPDATE messages m SET m.read = true WHERE m.conversation.id = :conversationId AND m.sender.userId != :userId AND m.read = false ")
    int markMessagesAsRead(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
}
