package com.zerohundreds.hunt.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cinematic_library_entries")
public class CinematicLibraryEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 80)
    private String category; // CINEMATOGRAPHY, EDITING, SOUND_DESIGN, LIGHTING, SHOT_COMPOSITION, DIRECTORS_NOTES, LESSONS

    @Column(name = "film_reference", length = 150)
    private String filmReference;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "personal_observations", columnDefinition = "MEDIUMTEXT", nullable = false)
    private String personalObservations;

    @Column(length = 255)
    private String tags;

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

    public CinematicLibraryEntry() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getFilmReference() { return filmReference; }
    public void setFilmReference(String filmReference) { this.filmReference = filmReference; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPersonalObservations() { return personalObservations; }
    public void setPersonalObservations(String personalObservations) { this.personalObservations = personalObservations; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
