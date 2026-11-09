package com.zerohundreds.hunt.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "storyboard_frames")
public class StoryboardFrame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storyboard_id", nullable = false)
    private Storyboard storyboard;

    @Column(name = "scene_number", nullable = false)
    private Integer sceneNumber = 1;

    @Column(name = "scene_heading", length = 255)
    private String sceneHeading;

    @Column(name = "frame_number", nullable = false)
    private Integer frameNumber = 1;

    @Column(name = "shot_type", length = 50)
    private String shotType = "Wide shot"; // Wide shot, Close-up, Over-the-shoulder, Medium shot, Long shot, etc.

    @Column(name = "camera_angle", length = 50)
    private String cameraAngle = "Eye Level"; // Eye Level, Low Angle, High Angle, Dutch Angle

    @Column(name = "camera_movement", length = 50)
    private String cameraMovement = "Static"; // Static, Pan, Dolly In, Tracking, Tilt

    @Column(name = "lighting", length = 100)
    private String lighting = "Natural / Dramatic";

    @Column(name = "character_details", columnDefinition = "TEXT")
    private String characterDetails;

    @Column(name = "shot_description", columnDefinition = "TEXT")
    private String shotDescription;

    @Column(name = "dialogue_reference", columnDefinition = "TEXT")
    private String dialogueReference;

    @Column(name = "image_url", columnDefinition = "LONGTEXT")
    private String imageUrl; // Can store SVG data URI, image URL, or base64 generated sketch

    @Column(name = "directors_notes", columnDefinition = "TEXT")
    private String directorsNotes;

    @Column(name = "display_order")
    private Integer displayOrder = 1;

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

    public StoryboardFrame() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Storyboard getStoryboard() { return storyboard; }
    public void setStoryboard(Storyboard storyboard) { this.storyboard = storyboard; }

    public Integer getSceneNumber() { return sceneNumber; }
    public void setSceneNumber(Integer sceneNumber) { this.sceneNumber = sceneNumber; }

    public String getSceneHeading() { return sceneHeading; }
    public void setSceneHeading(String sceneHeading) { this.sceneHeading = sceneHeading; }

    public Integer getFrameNumber() { return frameNumber; }
    public void setFrameNumber(Integer frameNumber) { this.frameNumber = frameNumber; }

    public String getShotType() { return shotType; }
    public void setShotType(String shotType) { this.shotType = shotType; }

    public String getCameraAngle() { return cameraAngle; }
    public void setCameraAngle(String cameraAngle) { this.cameraAngle = cameraAngle; }

    public String getCameraMovement() { return cameraMovement; }
    public void setCameraMovement(String cameraMovement) { this.cameraMovement = cameraMovement; }

    public String getLighting() { return lighting; }
    public void setLighting(String lighting) { this.lighting = lighting; }

    public String getCharacterDetails() { return characterDetails; }
    public void setCharacterDetails(String characterDetails) { this.characterDetails = characterDetails; }

    public String getShotDescription() { return shotDescription; }
    public void setShotDescription(String shotDescription) { this.shotDescription = shotDescription; }

    public String getDialogueReference() { return dialogueReference; }
    public void setDialogueReference(String dialogueReference) { this.dialogueReference = dialogueReference; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getDirectorsNotes() { return directorsNotes; }
    public void setDirectorsNotes(String directorsNotes) { this.directorsNotes = directorsNotes; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
