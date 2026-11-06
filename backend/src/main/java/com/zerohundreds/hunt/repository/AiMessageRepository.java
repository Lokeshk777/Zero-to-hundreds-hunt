package com.zerohundreds.hunt.repository;

import com.zerohundreds.hunt.entity.AiConversation;
import com.zerohundreds.hunt.entity.AiMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiMessageRepository extends JpaRepository<AiMessage, Long> {
    List<AiMessage> findByConversationOrderByCreatedAtAsc(AiConversation conversation);
}
