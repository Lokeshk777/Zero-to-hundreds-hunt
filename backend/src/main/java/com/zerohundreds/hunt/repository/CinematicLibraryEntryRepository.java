package com.zerohundreds.hunt.repository;

import com.zerohundreds.hunt.entity.CinematicLibraryEntry;
import com.zerohundreds.hunt.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CinematicLibraryEntryRepository extends JpaRepository<CinematicLibraryEntry, Long> {
    List<CinematicLibraryEntry> findByUserOrderByUpdatedAtDesc(User user);
    Optional<CinematicLibraryEntry> findByIdAndUser(Long id, User user);
    List<CinematicLibraryEntry> findByUserAndCategoryOrderByUpdatedAtDesc(User user, String category);
    long countByUser(User user);

    @Query("SELECT e FROM CinematicLibraryEntry e WHERE e.user = :user AND " +
           "(LOWER(e.title) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(e.filmReference) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(e.tags) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(e.description) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(e.personalObservations) LIKE LOWER(CONCAT('%', :q, '%'))) " +
           "ORDER BY e.updatedAt DESC")
    List<CinematicLibraryEntry> searchEntries(@Param("user") User user, @Param("q") String query);
}
