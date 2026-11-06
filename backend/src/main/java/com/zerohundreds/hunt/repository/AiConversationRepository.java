package com.zerohundreds.hunt.repository;

import com.zerohundreds.hunt.entity.AiConversation;
import com.zerohundreds.hunt.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiConversationRepository extends JpaRepository<AiConversation, Long> {
    List<AiConversation> findByUserOrderByUpdatedAtDesc(User user);
    Optional<AiConversation> findByIdAndUser(Long id, User user);
}
