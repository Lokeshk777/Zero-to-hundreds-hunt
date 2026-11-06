package com.zerohundreds.hunt.repository;

import com.zerohundreds.hunt.entity.ScreenplayProject;
import com.zerohundreds.hunt.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScreenplayProjectRepository extends JpaRepository<ScreenplayProject, Long> {
    List<ScreenplayProject> findByUserOrderByUpdatedAtDesc(User user);
    Optional<ScreenplayProject> findByIdAndUser(Long id, User user);
    long countByUser(User user);
    List<ScreenplayProject> findTop5ByUserOrderByUpdatedAtDesc(User user);
}
