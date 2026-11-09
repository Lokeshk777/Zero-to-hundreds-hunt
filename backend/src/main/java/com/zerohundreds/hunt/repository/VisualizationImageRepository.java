package com.zerohundreds.hunt.repository;

import com.zerohundreds.hunt.entity.VisualizationImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VisualizationImageRepository extends JpaRepository<VisualizationImage, Long> {
    List<VisualizationImage> findByVisualizationIdOrderByFrameOrderAsc(Long visualizationId);
    List<VisualizationImage> findBySceneIdOrderByFrameOrderAsc(Long sceneId);
    void deleteByVisualizationId(Long visualizationId);
}
