package com.zerohundreds.hunt.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public class ScreenplayDtos {

    public static class ScreenplayRequest {
        @NotBlank(message = "Title is required")
        private String title;
        private String genre;
        private String storyConcept;
        private String logline;
        private String synopsis;
        private String theme;
        private String centralConflict;
        private String structureType = "THREE_ACT";
        private String status = "DRAFT";

        public ScreenplayRequest() {}

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getGenre() { return genre; }
        public void setGenre(String genre) { this.genre = genre; }
        public String getStoryConcept() { return storyConcept; }
        public void setStoryConcept(String storyConcept) { this.storyConcept = storyConcept; }
        public String getLogline() { return logline; }
        public void setLogline(String logline) { this.logline = logline; }
        public String getSynopsis() { return synopsis; }
        public void setSynopsis(String synopsis) { this.synopsis = synopsis; }
        public String getTheme() { return theme; }
        public void setTheme(String theme) { this.theme = theme; }
        public String getCentralConflict() { return centralConflict; }
        public void setCentralConflict(String centralConflict) { this.centralConflict = centralConflict; }
        public String getStructureType() { return structureType; }
        public void setStructureType(String structureType) { this.structureType = structureType; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class ScreenplayResponse {
        private Long id;
        private String title;
        private String genre;
        private String storyConcept;
        private String logline;
        private String synopsis;
        private String theme;
        private String centralConflict;
        private String structureType;
        private String status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public ScreenplayResponse() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getGenre() { return genre; }
        public void setGenre(String genre) { this.genre = genre; }
        public String getStoryConcept() { return storyConcept; }
        public void setStoryConcept(String storyConcept) { this.storyConcept = storyConcept; }
        public String getLogline() { return logline; }
        public void setLogline(String logline) { this.logline = logline; }
        public String getSynopsis() { return synopsis; }
        public void setSynopsis(String synopsis) { this.synopsis = synopsis; }
        public String getTheme() { return theme; }
        public void setTheme(String theme) { this.theme = theme; }
        public String getCentralConflict() { return centralConflict; }
        public void setCentralConflict(String centralConflict) { this.centralConflict = centralConflict; }
        public String getStructureType() { return structureType; }
        public void setStructureType(String structureType) { this.structureType = structureType; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    }

    public static class CharacterRequest {
        @NotBlank(message = "Character name is required")
        private String name;
        private String roleType = "MAIN";
        private String age;
        private String background;
        private String personality;
        private String motivation;
        private String goals;
        private String strengths;
        private String weaknesses;
        private String internalConflict;
        private String externalConflict;
        private String characterArc;

        public CharacterRequest() {}

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
    }

    public static class SceneRequest {
        private Integer sceneNumber = 1;
        private String headingType = "INT.";
        @NotBlank(message = "Location is required")
        private String location;
        private String timeOfDay = "DAY";
        private String sceneHeading;
        private String description;
        private String characterAppearances;
        private String actions;
        private String dialogueContent;
        private String emotionalPurpose;
        private String sceneObjective;
        private String propsNeeded;
        private String equipmentNeeded;
        private String soundNotes;

        public SceneRequest() {}

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
    }

    public static class StructureStageRequest {
        private String structureName;
        private String stageName;
        private Integer stageOrder;
        private String description;
        private String notes;

        public StructureStageRequest() {}

        public String getStructureName() { return structureName; }
        public void setStructureName(String structureName) { this.structureName = structureName; }
        public String getStageName() { return stageName; }
        public void setStageName(String stageName) { this.stageName = stageName; }
        public Integer getStageOrder() { return stageOrder; }
        public void setStageOrder(Integer stageOrder) { this.stageOrder = stageOrder; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public static class DraftSaveRequest {
        private String rawContent;
        private Integer pageCount;

        public DraftSaveRequest() {}

        public String getRawContent() { return rawContent; }
        public void setRawContent(String rawContent) { this.rawContent = rawContent; }
        public Integer getPageCount() { return pageCount; }
        public void setPageCount(Integer pageCount) { this.pageCount = pageCount; }
    }
}
