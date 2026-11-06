package com.zerohundreds.hunt.dto;

import jakarta.validation.constraints.NotBlank;

public class VaultDtos {

    public static class IdeaRequest {
        @NotBlank(message = "Title is required")
        private String title;

        @NotBlank(message = "Category is required")
        private String category;

        @NotBlank(message = "Content is required")
        private String content;

        private String tags;
        private boolean favourite = false;

        public IdeaRequest() {}

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getTags() { return tags; }
        public void setTags(String tags) { this.tags = tags; }
        public boolean isFavourite() { return favourite; }
        public void setFavourite(boolean favourite) { this.favourite = favourite; }
    }
}
