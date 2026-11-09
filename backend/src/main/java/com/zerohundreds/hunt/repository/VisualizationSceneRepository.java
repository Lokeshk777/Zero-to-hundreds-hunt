package com.zerohundreds.hunt.repository;

import com.zerohundreds.hunt.entity.VisualizationScene;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VisualizationSceneRepository extends JpaRepository<VisualizationScene, Long> {
    List<VisualizationScene> findByVisualizationIdOrderBySceneNumberAsc(Long visualizationId);
    void deleteByVisualizationId(Long visualizationId);
}
