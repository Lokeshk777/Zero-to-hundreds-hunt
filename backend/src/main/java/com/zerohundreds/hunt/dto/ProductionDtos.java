package com.zerohundreds.hunt.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ProductionDtos {

    public static class ProjectRequest {
        @NotBlank(message = "Title is required")
        private String title;
        private Long screenplayId;
        private String stage = "DEVELOPMENT";
        private String description;
        private String director;
        private String producer;
        private LocalDate targetCompletionDate;
        private Integer progressPercentage = 0;

        public ProjectRequest() {}

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public Long getScreenplayId() { return screenplayId; }
        public void setScreenplayId(Long screenplayId) { this.screenplayId = screenplayId; }
        public String getStage() { return stage; }
        public void setStage(String stage) { this.stage = stage; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getDirector() { return director; }
        public void setDirector(String director) { this.director = director; }
        public String getProducer() { return producer; }
        public void setProducer(String producer) { this.producer = producer; }
        public LocalDate getTargetCompletionDate() { return targetCompletionDate; }
        public void setTargetCompletionDate(LocalDate targetCompletionDate) { this.targetCompletionDate = targetCompletionDate; }
        public Integer getProgressPercentage() { return progressPercentage; }
        public void setProgressPercentage(Integer progressPercentage) { this.progressPercentage = progressPercentage; }
    }

    public static class TaskRequest {
        @NotBlank(message = "Stage is required")
        private String stage;
        @NotBlank(message = "Task title is required")
        private String title;
        private String description;
        private String status = "PENDING";
        private LocalDate dueDate;
        private String priority = "MEDIUM";

        public TaskRequest() {}

        public String getStage() { return stage; }
        public void setStage(String stage) { this.stage = stage; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public LocalDate getDueDate() { return dueDate; }
        public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
    }
}
