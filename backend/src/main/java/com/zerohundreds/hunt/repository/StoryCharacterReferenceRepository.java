package com.zerohundreds.hunt.repository;

import com.zerohundreds.hunt.entity.StoryCharacterReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoryCharacterReferenceRepository extends JpaRepository<StoryCharacterReference, Long> {
    List<StoryCharacterReference> findByVisualizationIdOrderByIdAsc(Long visualizationId);
    void deleteByVisualizationId(Long visualizationId);
}
