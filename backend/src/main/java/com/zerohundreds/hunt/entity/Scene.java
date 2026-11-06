package com.zerohundreds.hunt.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "scenes")
public class Scene {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screenplay_id", nullable = false)
    private ScreenplayProject screenplay;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "scene_number", nullable = false)
    private Integer sceneNumber;

    @Column(name = "heading_type", length = 10)
    private String headingType = "INT."; // INT. or EXT.

    @Column(nullable = false, length = 150)
    private String location;

    @Column(name = "time_of_day", length = 50)
    private String timeOfDay = "DAY"; // DAY, NIGHT, DUSK, DAWN, CONTINUOUS

    @Column(name = "scene_heading", nullable = false, length = 255)
    private String sceneHeading;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "character_appearances", columnDefinition = "TEXT")
    private String characterAppearances; // Cast / Humans needed

    @Column(columnDefinition = "TEXT")
    private String actions;

    @Column(name = "dialogue_content", columnDefinition = "MEDIUMTEXT")
    private String dialogueContent;

    @Column(name = "emotional_purpose", columnDefinition = "TEXT")
    private String emotionalPurpose; // Importance of the scene

    @Column(name = "scene_objective", columnDefinition = "TEXT")
    private String sceneObjective;

    @Column(name = "props_needed", columnDefinition = "TEXT")
    private String propsNeeded; // Physical things / props needed

    @Column(name = "equipment_needed", columnDefinition = "TEXT")
    private String equipmentNeeded; // Camera, lighting, grip needed

    @Column(name = "sound_notes", columnDefinition = "TEXT")
    private String soundNotes; // Audio / Foley / Atmospheric requirements

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.sceneHeading == null || this.sceneHeading.isBlank()) {
            this.sceneHeading = this.headingType + " " + this.location + " - " + this.timeOfDay;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Scene() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ScreenplayProject getScreenplay() { return screenplay; }
    public void setScreenplay(ScreenplayProject screenplay) { this.screenplay = screenplay; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Integer getSceneNumber() { return sceneNumber; }
    public void setSceneNumber(Integer sceneNumber) { this.sceneNumber = sceneNumber; }

    public String getHeadingType() { return headingType; }
    public void setHeadingType(String headingType) { this.headingType = headingType; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getTimeOfDay() { return timeOfDay; }
    public void setTimeOfDay(String timeOfDay) { this.timeOfDay = timeOfDay; }

    public String getSceneHeading() { return sceneHeading; }
    public void setSceneHeading(String sceneHeading) { this.sceneHeading = sceneHeading; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCharacterAppearances() { return characterAppearances; }
    public void setCharacterAppearances(String characterAppearances) { this.characterAppearances = characterAppearances; }

    public String getActions() { return actions; }
    public void setActions(String actions) { this.actions = actions; }

    public String getDialogueContent() { return dialogueContent; }
    public void setDialogueContent(String dialogueContent) { this.dialogueContent = dialogueContent; }

    public String getEmotionalPurpose() { return emotionalPurpose; }
    public void setEmotionalPurpose(String emotionalPurpose) { this.emotionalPurpose = emotionalPurpose; }

    public String getSceneObjective() { return sceneObjective; }
    public void setSceneObjective(String sceneObjective) { this.sceneObjective = sceneObjective; }

    public String getPropsNeeded() { return propsNeeded; }
    public void setPropsNeeded(String propsNeeded) { this.propsNeeded = propsNeeded; }

    public String getEquipmentNeeded() { return equipmentNeeded; }
    public void setEquipmentNeeded(String equipmentNeeded) { this.equipmentNeeded = equipmentNeeded; }

    public String getSoundNotes() { return soundNotes; }
    public void setSoundNotes(String soundNotes) { this.soundNotes = soundNotes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
