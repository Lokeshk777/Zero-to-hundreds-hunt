package com.zerohundreds.hunt.repository;

import com.zerohundreds.hunt.entity.ScreenplayProject;
import com.zerohundreds.hunt.entity.StoryStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoryStructureRepository extends JpaRepository<StoryStructure, Long> {
    List<StoryStructure> findByScreenplayOrderByStageOrderAsc(ScreenplayProject screenplay);

    @Modifying
    @Query("DELETE FROM StoryStructure s WHERE s.screenplay = :screenplay")
    void deleteByScreenplay(@Param("screenplay") ScreenplayProject screenplay);
}
