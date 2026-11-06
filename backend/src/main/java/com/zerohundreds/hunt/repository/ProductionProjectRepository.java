package com.zerohundreds.hunt.repository;

import com.zerohundreds.hunt.entity.ProductionProject;
import com.zerohundreds.hunt.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductionProjectRepository extends JpaRepository<ProductionProject, Long> {
    List<ProductionProject> findByUserOrderByUpdatedAtDesc(User user);
    Optional<ProductionProject> findByIdAndUser(Long id, User user);
    long countByUser(User user);
    long countByUserAndStageNot(User user, String stage);
}
