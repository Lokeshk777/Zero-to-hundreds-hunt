package com.zerohundreds.hunt.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "characters")
public class CharacterProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screenplay_id", nullable = false)
    private ScreenplayProject screenplay;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "role_type", length = 50)
    private String roleType = "MAIN"; // MAIN, SUPPORTING, ANTAGONIST, MINOR

    @Column(length = 50)
    private String age;

    @Column(columnDefinition = "TEXT")
    private String background;

    @Column(columnDefinition = "TEXT")
    private String personality;

    @Column(columnDefinition = "TEXT")
    private String motivation;

    @Column(columnDefinition = "TEXT")
    private String goals;

    @Column(columnDefinition = "TEXT")
    private String strengths;

    @Column(columnDefinition = "TEXT")
    private String weaknesses;

    @Column(name = "internal_conflict", columnDefinition = "TEXT")
    private String internalConflict;

    @Column(name = "external_conflict", columnDefinition = "TEXT")
    private String externalConflict;

    @Column(name = "character_arc", columnDefinition = "TEXT")
    private String characterArc;

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

    public CharacterProfile() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ScreenplayProject getScreenplay() { return screenplay; }
    public void setScreenplay(ScreenplayProject screenplay) { this.screenplay = screenplay; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRoleType() { return roleType; }
    public void setRoleType(String roleType) { this.roleType = roleType; }

    public String getAge() { return age; }
    public void setAge(String age) { this.age = age; }

    public String getBackground() { return background; }
    public void setBackground(String background) { this.background = background; }

    public String getPersonality() { return personality; }
    public void setPersonality(String personality) { this.personality = personality; }

    public String getMotivation() { return motivation; }
    public void setMotivation(String motivation) { this.motivation = motivation; }

    public String getGoals() { return goals; }
    public void setGoals(String goals) { this.goals = goals; }

    public String getStrengths() { return strengths; }
    public void setStrengths(String strengths) { this.strengths = strengths; }

    public String getWeaknesses() { return weaknesses; }
    public void setWeaknesses(String weaknesses) { this.weaknesses = weaknesses; }

    public String getInternalConflict() { return internalConflict; }
    public void setInternalConflict(String internalConflict) { this.internalConflict = internalConflict; }

    public String getExternalConflict() { return externalConflict; }
    public void setExternalConflict(String externalConflict) { this.externalConflict = externalConflict; }

    public String getCharacterArc() { return characterArc; }
    public void setCharacterArc(String characterArc) { this.characterArc = characterArc; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
