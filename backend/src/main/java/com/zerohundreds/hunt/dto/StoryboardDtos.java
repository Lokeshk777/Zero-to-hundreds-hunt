package com.zerohundreds.hunt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class StoryboardDtos {

    public static class GenerateStoryboardRequest {
        @NotNull(message = "Screenplay ID is required")
        private Long screenplayId;
        private String style = "PENCIL_SKETCH"; // PENCIL_SKETCH, CHARCOAL, NOIR
        private Boolean createNewVersion = false;

        public Long getScreenplayId() { return screenplayId; }
        public void setScreenplayId(Long screenplayId) { this.screenplayId = screenplayId; }
        public String getStyle() { return style; }
        public void setStyle(String style) { this.style = style; }
        public Boolean getCreateNewVersion() { return createNewVersion; }
        public void setCreateNewVersion(Boolean createNewVersion) { this.createNewVersion = createNewVersion; }
    }

    public static class UpdateFrameRequest {
        private String shotType;
        private String cameraAngle;
        private String cameraMovement;
        private String lighting;
        private String shotDescription;
        private String dialogueReference;
        private String directorsNotes;
        private String imageUrl;

        public String getShotType() { return shotType; }
        public void setShotType(String shotType) { this.shotType = shotType; }
        public String getCameraAngle() { return cameraAngle; }
        public void setCameraAngle(String cameraAngle) { this.cameraAngle = cameraAngle; }
        public String getCameraMovement() { return cameraMovement; }
        public void setCameraMovement(String cameraMovement) { this.cameraMovement = cameraMovement; }
        public String getLighting() { return lighting; }
        public void setLighting(String lighting) { this.lighting = lighting; }
        public String getShotDescription() { return shotDescription; }
        public void setShotDescription(String shotDescription) { this.shotDescription = shotDescription; }
        public String getDialogueReference() { return dialogueReference; }
        public void setDialogueReference(String dialogueReference) { this.dialogueReference = dialogueReference; }
        public String getDirectorsNotes() { return directorsNotes; }
        public void setDirectorsNotes(String directorsNotes) { this.directorsNotes = directorsNotes; }
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    }

    public static class ReorderFramesRequest {
        private List<Long> frameIdsInOrder;

        public List<Long> getFrameIdsInOrder() { return frameIdsInOrder; }
        public void setFrameIdsInOrder(List<Long> frameIdsInOrder) { this.frameIdsInOrder = frameIdsInOrder; }
    }

    public static class StoryboardDetailResponse {
        private Long id;
        private Long screenplayId;
        private String screenplayTitle;
        private String genre;
        private Integer versionNumber;
        private String versionName;
        private Integer totalScenes;
        private Integer totalFrames;
        private String status;
        private String directorsNotes;
        private String createdAt;
        private String updatedAt;
        private List<com.zerohundreds.hunt.entity.StoryboardFrame> frames;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getScreenplayId() { return screenplayId; }
        public void setScreenplayId(Long screenplayId) { this.screenplayId = screenplayId; }
        public String getScreenplayTitle() { return screenplayTitle; }
        public void setScreenplayTitle(String screenplayTitle) { this.screenplayTitle = screenplayTitle; }
        public String getGenre() { return genre; }
        public void setGenre(String genre) { this.genre = genre; }
        public Integer getVersionNumber() { return versionNumber; }
        public void setVersionNumber(Integer versionNumber) { this.versionNumber = versionNumber; }
        public String getVersionName() { return versionName; }
        public void setVersionName(String versionName) { this.versionName = versionName; }
        public Integer getTotalScenes() { return totalScenes; }
        public void setTotalScenes(Integer totalScenes) { this.totalScenes = totalScenes; }
        public Integer getTotalFrames() { return totalFrames; }
        public void setTotalFrames(Integer totalFrames) { this.totalFrames = totalFrames; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getDirectorsNotes() { return directorsNotes; }
        public void setDirectorsNotes(String directorsNotes) { this.directorsNotes = directorsNotes; }
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
        public String getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
        public List<com.zerohundreds.hunt.entity.StoryboardFrame> getFrames() { return frames; }
        public void setFrames(List<com.zerohundreds.hunt.entity.StoryboardFrame> frames) { this.frames = frames; }
    }
}
