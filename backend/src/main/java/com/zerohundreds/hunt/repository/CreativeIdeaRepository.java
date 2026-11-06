package com.zerohundreds.hunt.repository;

import com.zerohundreds.hunt.entity.CreativeIdea;
import com.zerohundreds.hunt.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CreativeIdeaRepository extends JpaRepository<CreativeIdea, Long> {
    List<CreativeIdea> findByUserOrderByUpdatedAtDesc(User user);
    Optional<CreativeIdea> findByIdAndUser(Long id, User user);
    List<CreativeIdea> findByUserAndIsFavouriteTrueOrderByUpdatedAtDesc(User user);
    List<CreativeIdea> findByUserAndCategoryOrderByUpdatedAtDesc(User user, String category);
    long countByUser(User user);
    List<CreativeIdea> findTop5ByUserOrderByUpdatedAtDesc(User user);

    @Query("SELECT i FROM CreativeIdea i WHERE i.user = :user AND " +
           "(LOWER(i.title) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(i.tags) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(i.content) LIKE LOWER(CONCAT('%', :q, '%'))) " +
           "ORDER BY i.updatedAt DESC")
    List<CreativeIdea> searchIdeas(@Param("user") User user, @Param("q") String query);
}
