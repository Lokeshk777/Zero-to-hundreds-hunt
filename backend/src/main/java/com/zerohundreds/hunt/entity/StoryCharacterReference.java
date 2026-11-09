package com.zerohundreds.hunt.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "story_character_references")
public class StoryCharacterReference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visualization_id", nullable = false)
    private StoryVisualization visualization;

    @Column(name = "character_name", nullable = false, length = 150)
    private String characterName;

    @Column(name = "role_type", length = 50)
    private String roleType = "PROTAGONIST"; // PROTAGONIST, ANTAGONIST, SUPPORTING, MENTOR

    @Column(name = "approx_age", length = 50)
    private String approxAge = "Adult (30s)";

    @Column(name = "gender_presentation", length = 50)
    private String genderPresentation = "Not Specified";

    @Column(name = "facial_features", columnDefinition = "TEXT")
    private String facialFeatures;

    @Column(name = "hairstyle", length = 100)
    private String hairstyle;

    @Column(name = "hair_color", length = 50)
    private String hairColor;

    @Column(name = "clothing_continuity", columnDefinition = "TEXT")
    private String clothingContinuity;

    @Column(name = "distinctive_props", columnDefinition = "TEXT")
    private String distinctiveProps;

    @Column(name = "storage_path", length = 500)
    private String storagePath; // Path on physical disk or object key

    @Column(name = "reference_image_url", columnDefinition = "LONGTEXT")
    private String referenceImageUrl; // URL for frontend rendering

    @Column(name = "image_provenance", length = 50)
    private String imageProvenance = "LOCAL_ILLUSTRATION"; // AI_GENERATED, LOCAL_ILLUSTRATION, USER_UPLOADED

    @Column(name = "generation_prompt", columnDefinition = "TEXT")
    private String generationPrompt;

    @Column(name = "is_approved", nullable = false)
    private Boolean isApproved = false;

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

    public StoryCharacterReference() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public StoryVisualization getVisualization() { return visualization; }
    public void setVisualization(StoryVisualization visualization) { this.visualization = visualization; }

    public String getCharacterName() { return characterName; }
    public void setCharacterName(String characterName) { this.characterName = characterName; }

    public String getRoleType() { return roleType; }
    public void setRoleType(String roleType) { this.roleType = roleType; }

    public String getApproxAge() { return approxAge; }
    public void setApproxAge(String approxAge) { this.approxAge = approxAge; }

    public String getGenderPresentation() { return genderPresentation; }
    public void setGenderPresentation(String genderPresentation) { this.genderPresentation = genderPresentation; }

    public String getFacialFeatures() { return facialFeatures; }
    public void setFacialFeatures(String facialFeatures) { this.facialFeatures = facialFeatures; }

    public String getHairstyle() { return hairstyle; }
    public void setHairstyle(String hairstyle) { this.hairstyle = hairstyle; }

    public String getHairColor() { return hairColor; }
    public void setHairColor(String hairColor) { this.hairColor = hairColor; }

    public String getClothingContinuity() { return clothingContinuity; }
    public void setClothingContinuity(String clothingContinuity) { this.clothingContinuity = clothingContinuity; }

    public String getDistinctiveProps() { return distinctiveProps; }
    public void setDistinctiveProps(String distinctiveProps) { this.distinctiveProps = distinctiveProps; }

    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }

    public String getReferenceImageUrl() { return referenceImageUrl; }
    public void setReferenceImageUrl(String referenceImageUrl) { this.referenceImageUrl = referenceImageUrl; }

    public String getImageProvenance() { return imageProvenance; }
    public void setImageProvenance(String imageProvenance) { this.imageProvenance = imageProvenance; }

    public String getGenerationPrompt() { return generationPrompt; }
    public void setGenerationPrompt(String generationPrompt) { this.generationPrompt = generationPrompt; }

    public Boolean getIsApproved() { return isApproved; }
    public void setIsApproved(Boolean isApproved) { this.isApproved = isApproved; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
