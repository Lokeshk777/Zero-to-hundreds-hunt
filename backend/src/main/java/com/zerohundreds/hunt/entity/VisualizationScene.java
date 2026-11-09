package com.zerohundreds.hunt.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "visualization_scenes")
public class VisualizationScene {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visualization_id", nullable = false)
    private StoryVisualization visualization;

    @Column(name = "scene_number", nullable = false)
    private Integer sceneNumber;

    @Column(name = "scene_heading", nullable = false, length = 255)
    private String sceneHeading;

    @Column(name = "location_name", length = 150)
    private String locationName;

    @Column(name = "time_of_day", length = 50)
    private String timeOfDay = "DAY";

    @Column(name = "characters_present", columnDefinition = "TEXT")
    private String charactersPresent;

    @Column(name = "dramatic_action", columnDefinition = "LONGTEXT")
    private String dramaticAction;

    @Column(name = "emotional_tone", length = 100)
    private String emotionalTone;

    @Column(name = "suggested_camera_angle", length = 50)
    private String suggestedCameraAngle = "Eye Level";

    @Column(name = "camera_movement", length = 50)
    private String cameraMovement = "Static";

    @Column(name = "narrative_transition", length = 100)
    private String narrativeTransition = "Cut To";

    @Column(name = "chronology_order")
    private Integer chronologyOrder;

    @Column(name = "is_flashback")
    private Boolean isFlashback = false;

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

    public VisualizationScene() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public StoryVisualization getVisualization() { return visualization; }
    public void setVisualization(StoryVisualization visualization) { this.visualization = visualization; }

    public Integer getSceneNumber() { return sceneNumber; }
    public void setSceneNumber(Integer sceneNumber) { this.sceneNumber = sceneNumber; }

    public String getSceneHeading() { return sceneHeading; }
    public void setSceneHeading(String sceneHeading) { this.sceneHeading = sceneHeading; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public String getTimeOfDay() { return timeOfDay; }
    public void setTimeOfDay(String timeOfDay) { this.timeOfDay = timeOfDay; }

    public String getCharactersPresent() { return charactersPresent; }
    public void setCharactersPresent(String charactersPresent) { this.charactersPresent = charactersPresent; }

    public String getDramaticAction() { return dramaticAction; }
    public void setDramaticAction(String dramaticAction) { this.dramaticAction = dramaticAction; }

    public String getEmotionalTone() { return emotionalTone; }
    public void setEmotionalTone(String emotionalTone) { this.emotionalTone = emotionalTone; }

    public String getSuggestedCameraAngle() { return suggestedCameraAngle; }
    public void setSuggestedCameraAngle(String suggestedCameraAngle) { this.suggestedCameraAngle = suggestedCameraAngle; }

    public String getCameraMovement() { return cameraMovement; }
    public void setCameraMovement(String cameraMovement) { this.cameraMovement = cameraMovement; }

    public String getNarrativeTransition() { return narrativeTransition; }
    public void setNarrativeTransition(String narrativeTransition) { this.narrativeTransition = narrativeTransition; }

    public Integer getChronologyOrder() { return chronologyOrder; }
    public void setChronologyOrder(Integer chronologyOrder) { this.chronologyOrder = chronologyOrder; }

    public Boolean getIsFlashback() { return isFlashback; }
    public void setIsFlashback(Boolean isFlashback) { this.isFlashback = isFlashback; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
