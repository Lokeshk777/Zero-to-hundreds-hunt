package com.zerohundreds.hunt.repository;

import com.zerohundreds.hunt.entity.ScreenplayDraft;
import com.zerohundreds.hunt.entity.ScreenplayProject;
import com.zerohundreds.hunt.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ScreenplayDraftRepository extends JpaRepository<ScreenplayDraft, Long> {
    Optional<ScreenplayDraft> findByScreenplayAndUser(ScreenplayProject screenplay, User user);
    Optional<ScreenplayDraft> findByScreenplay(ScreenplayProject screenplay);

    @Modifying
    @Query("DELETE FROM ScreenplayDraft d WHERE d.screenplay = :screenplay")
    void deleteByScreenplay(@Param("screenplay") ScreenplayProject screenplay);
}
