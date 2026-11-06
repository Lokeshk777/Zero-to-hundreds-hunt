package com.zerohundreds.hunt.repository;

import com.zerohundreds.hunt.entity.CharacterProfile;
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
public interface CharacterProfileRepository extends JpaRepository<CharacterProfile, Long> {
    List<CharacterProfile> findByScreenplayAndUserOrderByCreatedAtAsc(ScreenplayProject screenplay, User user);
    Optional<CharacterProfile> findByIdAndUser(Long id, User user);
    List<CharacterProfile> findByUser(User user);

    @Modifying
    @Query("DELETE FROM CharacterProfile c WHERE c.screenplay = :screenplay")
    void deleteByScreenplay(@Param("screenplay") ScreenplayProject screenplay);
}
