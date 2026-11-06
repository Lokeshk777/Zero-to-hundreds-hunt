package com.zerohundreds.hunt.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "screenplay_projects")
public class ScreenplayProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 100)
    private String genre;

    @Column(name = "story_concept", columnDefinition = "TEXT")
    private String storyConcept;

    @Column(columnDefinition = "TEXT")
    private String logline;

    @Column(columnDefinition = "TEXT")
    private String synopsis;

    @Column(length = 255)
    private String theme;

    @Column(name = "central_conflict", columnDefinition = "TEXT")
    private String centralConflict;

    @Column(name = "structure_type", length = 50)
    private String structureType = "THREE_ACT";

    @Column(length = 50)
    private String status = "DRAFT";

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public ScreenplayProject() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getStoryConcept() { return storyConcept; }
    public void setStoryConcept(String storyConcept) { this.storyConcept = storyConcept; }

    public String getLogline() { return logline; }
    public void setLogline(String logline) { this.logline = logline; }

    public String getSynopsis() { return synopsis; }
    public void setSynopsis(String synopsis) { this.synopsis = synopsis; }

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }

    public String getCentralConflict() { return centralConflict; }
    public void setCentralConflict(String centralConflict) { this.centralConflict = centralConflict; }

    public String getStructureType() { return structureType; }
    public void setStructureType(String structureType) { this.structureType = structureType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
