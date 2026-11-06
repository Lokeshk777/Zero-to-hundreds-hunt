package com.zerohundreds.hunt.service;

import com.zerohundreds.hunt.dto.ProductionDtos.*;
import com.zerohundreds.hunt.entity.ProductionProject;
import com.zerohundreds.hunt.entity.ProductionTask;
import com.zerohundreds.hunt.entity.ScreenplayProject;
import com.zerohundreds.hunt.entity.User;
import com.zerohundreds.hunt.exception.ResourceNotFoundException;
import com.zerohundreds.hunt.repository.ProductionProjectRepository;
import com.zerohundreds.hunt.repository.ProductionTaskRepository;
import com.zerohundreds.hunt.repository.ScreenplayProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductionService {

    private final ProductionProjectRepository projectRepository;
    private final ProductionTaskRepository taskRepository;
    private final ScreenplayProjectRepository screenplayRepository;
    private final AuthService authService;

    public ProductionService(ProductionProjectRepository projectRepository,
                             ProductionTaskRepository taskRepository,
                             ScreenplayProjectRepository screenplayRepository,
                             AuthService authService) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.screenplayRepository = screenplayRepository;
        this.authService = authService;
    }

    public List<ProductionProject> getAllProjects() {
        User user = authService.getCurrentUser();
        return projectRepository.findByUserOrderByUpdatedAtDesc(user);
    }

    public ProductionProject getProject(Long id) {
        User user = authService.getCurrentUser();
        return projectRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Production project not found: " + id));
    }

    @Transactional
    public ProductionProject createProject(ProjectRequest request) {
        User user = authService.getCurrentUser();
        ProductionProject p = new ProductionProject();
        p.setUser(user);
        p.setTitle(request.getTitle().trim());
        p.setStage(request.getStage() != null ? request.getStage() : "DEVELOPMENT");
        p.setDescription(request.getDescription());
        p.setDirector(request.getDirector());
        p.setProducer(request.getProducer());
        p.setTargetCompletionDate(request.getTargetCompletionDate());
        p.setProgressPercentage(request.getProgressPercentage() != null ? request.getProgressPercentage() : 0);

        if (request.getScreenplayId() != null) {
            ScreenplayProject sp = screenplayRepository.findByIdAndUser(request.getScreenplayId(), user).orElse(null);
            p.setScreenplay(sp);
        }

        ProductionProject saved = projectRepository.save(p);
        initDefaultPipelineTasks(saved, user);
        return saved;
    }

    @Transactional
    public ProductionProject updateProject(Long id, ProjectRequest request) {
        ProductionProject p = getProject(id);
        User user = authService.getCurrentUser();

        p.setTitle(request.getTitle().trim());
        if (request.getStage() != null) p.setStage(request.getStage());
        p.setDescription(request.getDescription());
        p.setDirector(request.getDirector());
        p.setProducer(request.getProducer());
        p.setTargetCompletionDate(request.getTargetCompletionDate());
        if (request.getProgressPercentage() != null) p.setProgressPercentage(request.getProgressPercentage());

        if (request.getScreenplayId() != null) {
            ScreenplayProject sp = screenplayRepository.findByIdAndUser(request.getScreenplayId(), user).orElse(null);
            p.setScreenplay(sp);
        } else {
            p.setScreenplay(null);
        }

        return projectRepository.save(p);
    }

    @Transactional
    public void deleteProject(Long id) {
        ProductionProject p = getProject(id);
        projectRepository.delete(p);
    }

    // --- Tasks ---

    public List<ProductionTask> getTasks(Long projectId, String stage) {
        ProductionProject project = getProject(projectId);
        User user = authService.getCurrentUser();
        if (stage != null && !stage.isBlank()) {
            return taskRepository.findByProjectAndStageAndUserOrderByDueDateAsc(project, stage, user);
        }
        return taskRepository.findByProjectAndUserOrderByDueDateAsc(project, user);
    }

    @Transactional
    public ProductionTask createTask(Long projectId, TaskRequest request) {
        ProductionProject project = getProject(projectId);
        User user = authService.getCurrentUser();

        ProductionTask task = new ProductionTask();
        task.setProject(project);
        task.setUser(user);
        task.setStage(request.getStage());
        task.setTitle(request.getTitle().trim());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus() != null ? request.getStatus() : "PENDING");
        task.setDueDate(request.getDueDate());
        task.setPriority(request.getPriority() != null ? request.getPriority() : "MEDIUM");

        ProductionTask saved = taskRepository.save(task);
        recalcProgress(project, user);
        return saved;
    }

    @Transactional
    public ProductionTask updateTask(Long projectId, Long taskId, TaskRequest request) {
        ProductionProject project = getProject(projectId);
        User user = authService.getCurrentUser();

        ProductionTask task = taskRepository.findByIdAndUser(taskId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        if (!task.getProject().getId().equals(project.getId())) {
            throw new ResourceNotFoundException("Task does not belong to this project");
        }

        task.setTitle(request.getTitle().trim());
        task.setDescription(request.getDescription());
        if (request.getStage() != null) task.setStage(request.getStage());
        if (request.getStatus() != null) task.setStatus(request.getStatus());
        task.setDueDate(request.getDueDate());
        if (request.getPriority() != null) task.setPriority(request.getPriority());

        ProductionTask saved = taskRepository.save(task);
        recalcProgress(project, user);
        return saved;
    }

    @Transactional
    public void deleteTask(Long projectId, Long taskId) {
        ProductionProject project = getProject(projectId);
        User user = authService.getCurrentUser();

        ProductionTask task = taskRepository.findByIdAndUser(taskId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        if (!task.getProject().getId().equals(project.getId())) {
            throw new ResourceNotFoundException("Task does not belong to this project");
        }
        taskRepository.delete(task);
        recalcProgress(project, user);
    }

    private void recalcProgress(ProductionProject project, User user) {
        long total = taskRepository.countByProjectAndUser(project, user);
        if (total == 0) return;
        long completed = taskRepository.countByProjectAndStatusAndUser(project, "COMPLETED", user);
        int pct = (int) Math.round(((double) completed / total) * 100);
        project.setProgressPercentage(pct);
        projectRepository.save(project);
    }

    private void initDefaultPipelineTasks(ProductionProject project, User user) {
        // Development
        createDefaultTask(project, user, "DEVELOPMENT", "Finalize Logline and Synopsis", "HIGH");
        createDefaultTask(project, user, "DEVELOPMENT", "Complete Beat Sheet Structure", "HIGH");
        createDefaultTask(project, user, "DEVELOPMENT", "First Draft Screenplay Lock", "MEDIUM");

        // Pre-production
        createDefaultTask(project, user, "PRE_PRODUCTION", "Casting Calls and Actor Auditions", "HIGH");
        createDefaultTask(project, user, "PRE_PRODUCTION", "Location Scouting and Permits", "HIGH");
        createDefaultTask(project, user, "PRE_PRODUCTION", "Camera and Lighting Gear Rental Lock", "MEDIUM");
        createDefaultTask(project, user, "PRE_PRODUCTION", "Master Shooting Schedule Breakdown", "URGENT");

        // Production
        createDefaultTask(project, user, "PRODUCTION", "Principal Photography - Day 1 Shoot", "URGENT");
        createDefaultTask(project, user, "PRODUCTION", "Audio Sync and Daily Footage Backup", "HIGH");

        // Post-production
        createDefaultTask(project, user, "POST_PRODUCTION", "Rough Cut Assembly Edit", "HIGH");
        createDefaultTask(project, user, "POST_PRODUCTION", "Foley, Dialogue cleanup & Original Score", "MEDIUM");
        createDefaultTask(project, user, "POST_PRODUCTION", "Color Grading & Master DCP Export", "HIGH");
    }

    private void createDefaultTask(ProductionProject project, User user, String stage, String title, String priority) {
        ProductionTask task = new ProductionTask();
        task.setProject(project);
        task.setUser(user);
        task.setStage(stage);
        task.setTitle(title);
        task.setStatus("PENDING");
        task.setPriority(priority);
        taskRepository.save(task);
    }
}
