package com.zerohundreds.hunt.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "story_visualizations")
public class StoryVisualization {

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

    @Column(name = "visual_style", length = 50)
    private String visualStyle = "Cinematic Photorealism";

    @Column(name = "aspect_ratio", length = 20)
    private String aspectRatio = "16:9";

    @Column(name = "analysis_provenance", length = 50)
    private String analysisProvenance = "GEMINI_AI"; // GEMINI_AI, LOCAL_PARSED

    @Column(name = "status", length = 50)
    private String status = "DRAFT"; // DRAFT, ANALYZED, GENERATING, COMPLETED, FAILED

    @Column(name = "story_logline", columnDefinition = "TEXT")
    private String storyLogline;

    @Column(name = "story_theme", length = 255)
    private String storyTheme;

    @Column(name = "story_synopsis", columnDefinition = "LONGTEXT")
    private String storySynopsis;

    @Column(name = "total_scenes")
    private Integer totalScenes = 0;

    @Column(name = "total_characters")
    private Integer totalCharacters = 0;

    @Column(name = "total_images")
    private Integer totalImages = 0;

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

    public StoryVisualization() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ScreenplayProject getScreenplay() { return screenplay; }
    public void setScreenplay(ScreenplayProject screenplay) { this.screenplay = screenplay; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getVisualStyle() { return visualStyle; }
    public void setVisualStyle(String visualStyle) { this.visualStyle = visualStyle; }

    public String getAspectRatio() { return aspectRatio; }
    public void setAspectRatio(String aspectRatio) { this.aspectRatio = aspectRatio; }

    public String getAnalysisProvenance() { return analysisProvenance; }
    public void setAnalysisProvenance(String analysisProvenance) { this.analysisProvenance = analysisProvenance; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStoryLogline() { return storyLogline; }
    public void setStoryLogline(String storyLogline) { this.storyLogline = storyLogline; }

    public String getStoryTheme() { return storyTheme; }
    public void setStoryTheme(String storyTheme) { this.storyTheme = storyTheme; }

    public String getStorySynopsis() { return storySynopsis; }
    public void setStorySynopsis(String storySynopsis) { this.storySynopsis = storySynopsis; }

    public Integer getTotalScenes() { return totalScenes; }
    public void setTotalScenes(Integer totalScenes) { this.totalScenes = totalScenes; }

    public Integer getTotalCharacters() { return totalCharacters; }
    public void setTotalCharacters(Integer totalCharacters) { this.totalCharacters = totalCharacters; }

    public Integer getTotalImages() { return totalImages; }
    public void setTotalImages(Integer totalImages) { this.totalImages = totalImages; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
