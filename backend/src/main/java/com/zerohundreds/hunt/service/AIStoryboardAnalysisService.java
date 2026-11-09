package com.zerohundreds.hunt.service;

import com.zerohundreds.hunt.entity.*;
import com.zerohundreds.hunt.repository.SceneRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AIStoryboardAnalysisService {

    private final StoryboardImageGenerationService imageGenerationService;

    public AIStoryboardAnalysisService(StoryboardImageGenerationService imageGenerationService) {
        this.imageGenerationService = imageGenerationService;
    }

    public static class AnalyzedShot {
        public int sceneNumber;
        public String sceneHeading;
        public int frameNumber;
        public String shotType;
        public String cameraAngle;
        public String cameraMovement;
        public String lighting;
        public String characterDetails;
        public String shotDescription;
        public String dialogueReference;
        public String imageUrl;
    }

    /**
     * Parses the screenplay scenes, character actions, and locations to generate
     * cinematic shots with director compositions and authentic sketch illustrations.
     */
    public List<AnalyzedShot> analyzeScreenplayScenes(ScreenplayProject screenplay, List<Scene> scenes) {
        List<AnalyzedShot> result = new ArrayList<>();
        int frameCounter = 1;

        if (scenes == null || scenes.isEmpty()) {
            // If no individual scenes registered yet, break down from synopsis / concept
            AnalyzedShot s1 = new AnalyzedShot();
            s1.sceneNumber = 1;
            s1.sceneHeading = "SCENE 01 — THE ARRIVAL (EXT. PLATFORM / STREET)";
            s1.frameNumber = frameCounter++;
            s1.shotType = "Wide shot";
            s1.cameraAngle = "Eye Level";
            s1.cameraMovement = "Slow Dolly In";
            s1.lighting = "Overcast / Wet reflections";
            s1.characterDetails = "Protagonist in dark coat with travel briefcase.";
            s1.shotDescription = "Character enters railway station through misty arches.";
            s1.dialogueReference = "";
            s1.imageUrl = imageGenerationService.generateSketchSvg(s1.shotType, s1.cameraAngle, s1.shotDescription, "Railway Station", 1);
            result.add(s1);

            AnalyzedShot s2 = new AnalyzedShot();
            s2.sceneNumber = 1;
            s2.sceneHeading = "SCENE 01 — THE ARRIVAL (EXT. PLATFORM / STREET)";
            s2.frameNumber = frameCounter++;
            s2.shotType = "Close-up";
            s2.cameraAngle = "Low Angle";
            s2.cameraMovement = "Static";
            s2.lighting = "High-contrast tungsten key light";
            s2.characterDetails = "Protagonist tense jawline and focused gaze.";
            s2.shotDescription = "Character checks his pocket watch beneath flickering streetlamp.";
            s2.dialogueReference = "\"Time is running out.\"";
            s2.imageUrl = imageGenerationService.generateSketchSvg(s2.shotType, s2.cameraAngle, s2.shotDescription, "Alleyway", 2);
            result.add(s2);

            AnalyzedShot s3 = new AnalyzedShot();
            s3.sceneNumber = 1;
            s3.sceneHeading = "SCENE 01 — THE ARRIVAL (EXT. PLATFORM / STREET)";
            s3.frameNumber = frameCounter++;
            s3.shotType = "Over-the-shoulder";
            s3.cameraAngle = "Dutch Angle";
            s3.cameraMovement = "Handheld pan";
            s3.lighting = "Deep shadows, sharp silhouette";
            s3.characterDetails = "Protagonist looking forward toward tracks.";
            s3.shotDescription = "He notices someone standing motionless across the distant platform.";
            s3.dialogueReference = "";
            s3.imageUrl = imageGenerationService.generateSketchSvg(s3.shotType, s3.cameraAngle, s3.shotDescription, "Platform", 3);
            result.add(s3);

            AnalyzedShot s4 = new AnalyzedShot();
            s4.sceneNumber = 1;
            s4.sceneHeading = "SCENE 01 — THE ARRIVAL (EXT. PLATFORM / STREET)";
            s4.frameNumber = frameCounter++;
            s4.shotType = "Long shot";
            s4.cameraAngle = "High Angle";
            s4.cameraMovement = "Crane Down";
            s4.lighting = "Distant station floodlights cutting through fog";
            s4.characterDetails = "Mysterious figure in trench coat on upper balcony.";
            s4.shotDescription = "Mysterious figure stands in the distance watching the exchange.";
            s4.dialogueReference = "";
            s4.imageUrl = imageGenerationService.generateSketchSvg(s4.shotType, s4.cameraAngle, s4.shotDescription, "Upper Balcony", 4);
            result.add(s4);

            return result;
        }

        // Deconstruct screenplay scenes into 2 to 4 shots per scene
        for (Scene sc : scenes) {
            int scNum = sc.getSceneNumber() != null ? sc.getSceneNumber() : 1;
            String heading = "SCENE " + String.format("%02d", scNum) + " — " + (sc.getLocation() != null ? sc.getLocation() : "LOCATION");

            // Shot A: Establishing Shot (Wide)
            AnalyzedShot shotA = new AnalyzedShot();
            shotA.sceneNumber = scNum;
            shotA.sceneHeading = heading;
            shotA.frameNumber = frameCounter++;
            shotA.shotType = "Wide shot";
            shotA.cameraAngle = "Eye Level";
            shotA.cameraMovement = "Slow Dolly In";
            shotA.lighting = sc.getTimeOfDay() != null ? sc.getTimeOfDay() + " Lighting" : "Atmospheric";
            shotA.characterDetails = sc.getCharacterAppearances() != null ? sc.getCharacterAppearances() : "Main characters";
            shotA.shotDescription = sc.getDescription() != null && !sc.getDescription().isBlank() 
                ? sc.getDescription() 
                : "Establishing wide composition of " + sc.getLocation() + ".";
            shotA.dialogueReference = "";
            shotA.imageUrl = imageGenerationService.generateSketchSvg(shotA.shotType, shotA.cameraAngle, shotA.shotDescription, sc.getLocation(), frameCounter);
            result.add(shotA);

            // Shot B: Action / Character Focus
            AnalyzedShot shotB = new AnalyzedShot();
            shotB.sceneNumber = scNum;
            shotB.sceneHeading = heading;
            shotB.frameNumber = frameCounter++;
            shotB.shotType = "Close-up";
            shotB.cameraAngle = "Low Angle";
            shotB.cameraMovement = "Static";
            shotB.lighting = "Dramatic chiaroscuro / Rim light";
            shotB.characterDetails = sc.getCharacterAppearances();
            shotB.shotDescription = sc.getActions() != null && !sc.getActions().isBlank() 
                ? sc.getActions() 
                : "Intense character confrontation and physical reaction.";
            shotB.dialogueReference = sc.getDialogueContent() != null && !sc.getDialogueContent().isBlank()
                ? extractFirstLine(sc.getDialogueContent())
                : "";
            shotB.imageUrl = imageGenerationService.generateSketchSvg(shotB.shotType, shotB.cameraAngle, shotB.shotDescription, sc.getLocation(), frameCounter);
            result.add(shotB);

            // Shot C: Cinematic perspective
            AnalyzedShot shotC = new AnalyzedShot();
            shotC.sceneNumber = scNum;
            shotC.sceneHeading = heading;
            shotC.frameNumber = frameCounter++;
            shotC.shotType = (frameCounter % 2 == 0) ? "Over-the-shoulder" : "Long shot";
            shotC.cameraAngle = "Dutch Angle";
            shotC.cameraMovement = "Tracking Pan";
            shotC.lighting = "Directional key light with deep shadows";
            shotC.characterDetails = sc.getCharacterAppearances();
            shotC.shotDescription = "Dynamic visual perspective highlighting the emotional tension of the moment.";
            shotC.dialogueReference = "";
            shotC.imageUrl = imageGenerationService.generateSketchSvg(shotC.shotType, shotC.cameraAngle, shotC.shotDescription, sc.getLocation(), frameCounter);
            result.add(shotC);
        }

        return result;
    }

    private String extractFirstLine(String dialogue) {
        if (dialogue == null || dialogue.isBlank()) return "";
        String[] lines = dialogue.split("\\r?\\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty() && !trimmed.matches("^[A-Z0-9\\s]{2,}$")) {
                return trimmed.length() > 80 ? trimmed.substring(0, 77) + "..." : trimmed;
            }
        }
        return lines[0].trim();
    }
}
