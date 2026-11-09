package com.zerohundreds.hunt.repository;

import com.zerohundreds.hunt.entity.StoryVisualization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoryVisualizationRepository extends JpaRepository<StoryVisualization, Long> {
    List<StoryVisualization> findByUserIdOrderByUpdatedAtDesc(Long userId);
    List<StoryVisualization> findByScreenplayIdOrderByUpdatedAtDesc(Long screenplayId);
    Optional<StoryVisualization> findTopByScreenplayIdOrderByUpdatedAtDesc(Long screenplayId);
}
