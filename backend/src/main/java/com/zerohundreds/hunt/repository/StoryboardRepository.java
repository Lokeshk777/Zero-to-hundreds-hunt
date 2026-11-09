package com.zerohundreds.hunt.repository;

import com.zerohundreds.hunt.entity.Storyboard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoryboardRepository extends JpaRepository<Storyboard, Long> {
    List<Storyboard> findByScreenplayIdOrderByVersionNumberDesc(Long screenplayId);
    List<Storyboard> findByUserIdOrderByUpdatedAtDesc(Long userId);
    Optional<Storyboard> findTopByScreenplayIdOrderByVersionNumberDesc(Long screenplayId);
}
