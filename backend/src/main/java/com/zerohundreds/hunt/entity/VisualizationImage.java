package com.zerohundreds.hunt.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "visualization_images")
public class VisualizationImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visualization_id", nullable = false)
    private StoryVisualization visualization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scene_id", nullable = false)
    private VisualizationScene scene;

    @Column(name = "frame_order", nullable = false)
    private Integer frameOrder = 1;

    @Column(name = "storage_path", length = 500)
    private String storagePath; // Path on physical disk: storage/story-visualizations/...

    @Column(name = "image_url", columnDefinition = "LONGTEXT")
    private String imageUrl; // Accessible serving URL

    @Column(name = "image_provenance", length = 50)
    private String imageProvenance = "LOCAL_ILLUSTRATION"; // AI_GENERATED, LOCAL_ILLUSTRATION, USER_UPLOADED

    @Column(name = "generation_status", length = 50)
    private String generationStatus = "COMPLETED"; // PENDING, GENERATING, COMPLETED, FAILED

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "generation_prompt", columnDefinition = "LONGTEXT")
    private String generationPrompt;

    @Column(name = "camera_shot_type", length = 50)
    private String cameraShotType = "Wide shot";

    @Column(name = "directors_notes", columnDefinition = "TEXT")
    private String directorsNotes;

    @Column(name = "version_number")
    private Integer versionNumber = 1;

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

    public VisualizationImage() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public StoryVisualization getVisualization() { return visualization; }
    public void setVisualization(StoryVisualization visualization) { this.visualization = visualization; }

    public VisualizationScene getScene() { return scene; }
    public void setScene(VisualizationScene scene) { this.scene = scene; }

    public Integer getFrameOrder() { return frameOrder; }
    public void setFrameOrder(Integer frameOrder) { this.frameOrder = frameOrder; }

    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getImageProvenance() { return imageProvenance; }
    public void setImageProvenance(String imageProvenance) { this.imageProvenance = imageProvenance; }

    public String getGenerationStatus() { return generationStatus; }
    public void setGenerationStatus(String generationStatus) { this.generationStatus = generationStatus; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public String getGenerationPrompt() { return generationPrompt; }
    public void setGenerationPrompt(String generationPrompt) { this.generationPrompt = generationPrompt; }

    public String getCameraShotType() { return cameraShotType; }
    public void setCameraShotType(String cameraShotType) { this.cameraShotType = cameraShotType; }

    public String getDirectorsNotes() { return directorsNotes; }
    public void setDirectorsNotes(String directorsNotes) { this.directorsNotes = directorsNotes; }

    public Integer getVersionNumber() { return versionNumber; }
    public void setVersionNumber(Integer versionNumber) { this.versionNumber = versionNumber; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
