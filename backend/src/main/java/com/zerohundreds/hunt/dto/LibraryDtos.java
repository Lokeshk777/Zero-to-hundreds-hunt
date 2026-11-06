package com.zerohundreds.hunt.dto;

import jakarta.validation.constraints.NotBlank;

public class LibraryDtos {

    public static class EntryRequest {
        @NotBlank(message = "Title is required")
        private String title;

        @NotBlank(message = "Category is required")
        private String category;

        private String filmReference;
        private String description;

        @NotBlank(message = "Personal observations are required")
        private String personalObservations;

        private String tags;

        public EntryRequest() {}

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
    }
}
