package com.zerohundreds.hunt.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "storyboards")
public class Storyboard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screenplay_id", nullable = false)
    private ScreenplayProject screenplay;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber = 1;

    @Column(name = "version_name", length = 100)
    private String versionName = "Initial Revision";

    @Column(name = "total_scenes")
    private Integer totalScenes = 0;

    @Column(name = "total_frames")
    private Integer totalFrames = 0;

    @Column(length = 50)
    private String status = "READY"; // DRAFT, GENERATING, READY, ARCHIVED

    @Column(name = "directors_notes", columnDefinition = "TEXT")
    private String directorsNotes;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Storyboard() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ScreenplayProject getScreenplay() { return screenplay; }
    public void setScreenplay(ScreenplayProject screenplay) { this.screenplay = screenplay; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Integer getVersionNumber() { return versionNumber; }
    public void setVersionNumber(Integer versionNumber) { this.versionNumber = versionNumber; }

    public String getVersionName() { return versionName; }
    public void setVersionName(String versionName) { this.versionName = versionName; }

    public Integer getTotalScenes() { return totalScenes; }
    public void setTotalScenes(Integer totalScenes) { this.totalScenes = totalScenes; }

    public Integer getTotalFrames() { return totalFrames; }
    public void setTotalFrames(Integer totalFrames) { this.totalFrames = totalFrames; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDirectorsNotes() { return directorsNotes; }
    public void setDirectorsNotes(String directorsNotes) { this.directorsNotes = directorsNotes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
