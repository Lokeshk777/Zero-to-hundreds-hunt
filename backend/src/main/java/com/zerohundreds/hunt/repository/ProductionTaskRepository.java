package com.zerohundreds.hunt.repository;

import com.zerohundreds.hunt.entity.ProductionProject;
import com.zerohundreds.hunt.entity.ProductionTask;
import com.zerohundreds.hunt.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductionTaskRepository extends JpaRepository<ProductionTask, Long> {
    List<ProductionTask> findByProjectAndUserOrderByDueDateAsc(ProductionProject project, User user);
    List<ProductionTask> findByProjectAndStageAndUserOrderByDueDateAsc(ProductionProject project, String stage, User user);
    Optional<ProductionTask> findByIdAndUser(Long id, User user);
    long countByProjectAndUser(ProductionProject project, User user);
    long countByProjectAndStatusAndUser(ProductionProject project, String status, User user);
    void deleteByProject(ProductionProject project);
}
