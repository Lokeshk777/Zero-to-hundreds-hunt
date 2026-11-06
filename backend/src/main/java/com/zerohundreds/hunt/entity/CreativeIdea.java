package com.zerohundreds.hunt.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "creative_ideas")
public class CreativeIdea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 80)
    private String category; // RAW_STORY, ONE_LINER, LOGLINE, CHARACTER_IDEA, PLOT_TWIST, SCENE_IDEA, DIALOGUE, TITLE, EXPERIMENTAL

    @Column(columnDefinition = "MEDIUMTEXT", nullable = false)
    private String content;

    @Column(length = 255)
    private String tags;

    @Column(name = "is_favourite", nullable = false)
    private boolean isFavourite = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "converted_to_screenplay_id")
    private ScreenplayProject convertedToScreenplay;

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

    public CreativeIdea() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public boolean isFavourite() { return isFavourite; }
    public void setFavourite(boolean favourite) { isFavourite = favourite; }

    public ScreenplayProject getConvertedToScreenplay() { return convertedToScreenplay; }
    public void setConvertedToScreenplay(ScreenplayProject convertedToScreenplay) { this.convertedToScreenplay = convertedToScreenplay; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
