package com.zerohundreds.hunt.repository;

import com.zerohundreds.hunt.entity.Scene;
import com.zerohundreds.hunt.entity.ScreenplayProject;
import com.zerohundreds.hunt.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SceneRepository extends JpaRepository<Scene, Long> {
    List<Scene> findByScreenplayAndUserOrderBySceneNumberAsc(ScreenplayProject screenplay, User user);
    Optional<Scene> findByIdAndUser(Long id, User user);
    long countByScreenplayAndUser(ScreenplayProject screenplay, User user);

    @Modifying
    @Query("DELETE FROM Scene s WHERE s.screenplay = :screenplay")
    void deleteByScreenplay(@Param("screenplay") ScreenplayProject screenplay);
}
