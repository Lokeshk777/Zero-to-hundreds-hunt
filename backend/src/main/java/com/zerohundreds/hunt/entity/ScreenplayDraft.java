package com.zerohundreds.hunt.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "screenplay_drafts")
public class ScreenplayDraft {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screenplay_id", nullable = false, unique = true)
    private ScreenplayProject screenplay;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "raw_content", columnDefinition = "MEDIUMTEXT")
    private String rawContent;

    @Column(name = "page_count")
    private Integer pageCount = 1;

    @Column(name = "last_saved")
    private LocalDateTime lastSaved;

    @PrePersist
    @PreUpdate
    protected void onSave() {
        this.lastSaved = LocalDateTime.now();
    }

    public ScreenplayDraft() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ScreenplayProject getScreenplay() { return screenplay; }
    public void setScreenplay(ScreenplayProject screenplay) { this.screenplay = screenplay; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getRawContent() { return rawContent; }
    public void setRawContent(String rawContent) { this.rawContent = rawContent; }

    public Integer getPageCount() { return pageCount; }
    public void setPageCount(Integer pageCount) { this.pageCount = pageCount; }

    public LocalDateTime getLastSaved() { return lastSaved; }
    public void setLastSaved(LocalDateTime lastSaved) { this.lastSaved = lastSaved; }
}
