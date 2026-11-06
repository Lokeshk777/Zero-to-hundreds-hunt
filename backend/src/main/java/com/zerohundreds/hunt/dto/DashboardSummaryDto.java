package com.zerohundreds.hunt.dto;

import java.util.List;

public class DashboardSummaryDto {
    private String userFullName;
    private String username;
    private long totalScreenplays;
    private long totalCreativeIdeas;
    private long activeProductionProjects;
    private long totalLibraryEntries;
    private List<ScreenplaySummary> recentScreenplays;
    private List<IdeaSummary> recentIdeas;
    private List<ProductionSummary> recentProductions;

    public DashboardSummaryDto() {}

    public static class ScreenplaySummary {
        private Long id;
        private String title;
        private String genre;
        private String status;
        private String updatedAt;

        public ScreenplaySummary(Long id, String title, String genre, String status, String updatedAt) {
            this.id = id;
            this.title = title;
            this.genre = genre;
            this.status = status;
            this.updatedAt = updatedAt;
        }

        public Long getId() { return id; }
        public String getTitle() { return title; }
        public String getGenre() { return genre; }
        public String getStatus() { return status; }
        public String getUpdatedAt() { return updatedAt; }
    }

    public static class IdeaSummary {
        private Long id;
        private String title;
        private String category;
        private boolean favourite;
        private String updatedAt;

        public IdeaSummary(Long id, String title, String category, boolean favourite, String updatedAt) {
            this.id = id;
            this.title = title;
            this.category = category;
            this.favourite = favourite;
            this.updatedAt = updatedAt;
        }

        public Long getId() { return id; }
        public String getTitle() { return title; }
        public String getCategory() { return category; }
        public boolean isFavourite() { return favourite; }
        public String getUpdatedAt() { return updatedAt; }
    }

    public static class ProductionSummary {
        private Long id;
        private String title;
        private String stage;
        private int progressPercentage;

        public ProductionSummary(Long id, String title, String stage, int progressPercentage) {
            this.id = id;
            this.title = title;
            this.stage = stage;
            this.progressPercentage = progressPercentage;
        }

        public Long getId() { return id; }
        public String getTitle() { return title; }
        public String getStage() { return stage; }
        public int getProgressPercentage() { return progressPercentage; }
    }

    // Getters and Setters
    public String getUserFullName() { return userFullName; }
    public void setUserFullName(String userFullName) { this.userFullName = userFullName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public long getTotalScreenplays() { return totalScreenplays; }
    public void setTotalScreenplays(long totalScreenplays) { this.totalScreenplays = totalScreenplays; }

    public long getTotalCreativeIdeas() { return totalCreativeIdeas; }
    public void setTotalCreativeIdeas(long totalCreativeIdeas) { this.totalCreativeIdeas = totalCreativeIdeas; }

    public long getActiveProductionProjects() { return activeProductionProjects; }
    public void setActiveProductionProjects(long activeProductionProjects) { this.activeProductionProjects = activeProductionProjects; }

    public long getTotalLibraryEntries() { return totalLibraryEntries; }
    public void setTotalLibraryEntries(long totalLibraryEntries) { this.totalLibraryEntries = totalLibraryEntries; }

    public List<ScreenplaySummary> getRecentScreenplays() { return recentScreenplays; }
    public void setRecentScreenplays(List<ScreenplaySummary> recentScreenplays) { this.recentScreenplays = recentScreenplays; }

    public List<IdeaSummary> getRecentIdeas() { return recentIdeas; }
    public void setRecentIdeas(List<IdeaSummary> recentIdeas) { this.recentIdeas = recentIdeas; }

    public List<ProductionSummary> getRecentProductions() { return recentProductions; }
    public void setRecentProductions(List<ProductionSummary> recentProductions) { this.recentProductions = recentProductions; }
}
