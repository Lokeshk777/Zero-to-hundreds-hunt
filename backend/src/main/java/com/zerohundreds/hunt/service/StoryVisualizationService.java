package com.zerohundreds.hunt.service;

import com.zerohundreds.hunt.entity.*;
import com.zerohundreds.hunt.exception.ResourceNotFoundException;
import com.zerohundreds.hunt.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StoryVisualizationService {

    private final StoryVisualizationRepository visualizationRepository;
    private final StoryCharacterReferenceRepository characterReferenceRepository;
    private final VisualizationSceneRepository sceneRepository;
    private final VisualizationImageRepository imageRepository;
    private final ScreenplayProjectRepository screenplayRepository;
    private final StoryAnalysisService analysisService;
    private final StoryChartGenerationService chartGenerationService;
    private final AIImageGenerationService imageGenService;
    private final ImageStorageService storageService;
    private final AuthService authService;

    public StoryVisualizationService(
            StoryVisualizationRepository visualizationRepository,
            StoryCharacterReferenceRepository characterReferenceRepository,
            VisualizationSceneRepository sceneRepository,
            VisualizationImageRepository imageRepository,
            ScreenplayProjectRepository screenplayRepository,
            StoryAnalysisService analysisService,
            StoryChartGenerationService chartGenerationService,
            AIImageGenerationService imageGenService,
            ImageStorageService storageService,
            AuthService authService) {
        this.visualizationRepository = visualizationRepository;
        this.characterReferenceRepository = characterReferenceRepository;
        this.sceneRepository = sceneRepository;
        this.imageRepository = imageRepository;
        this.screenplayRepository = screenplayRepository;
        this.analysisService = analysisService;
        this.chartGenerationService = chartGenerationService;
        this.imageGenService = imageGenService;
        this.storageService = storageService;
        this.authService = authService;
    }

    public static class FullVisualizationResponse {
        public StoryVisualization visualization;
        public List<StoryCharacterReference> characters;
        public List<VisualizationScene> scenes;
        public List<VisualizationImage> images;
        public Map<String, String> charts = new HashMap<>();
        public String quotaNotice;
    }

    /**
     * Start complete story visualization workflow:
     * 1. Retrieve saved screenplay from MySQL
     * 2. Analyze story (Gemini 3.8 Flash / verified local structured fallback)
     * 3. Persist character reference profiles
     * 4. Persist chronological visual scenes
     * 5. Generate actual physical scene images and store on disk
     * 6. Generate 5 interactive Mermaid charts
     */
    @Transactional
    public FullVisualizationResponse generateVisualization(Long screenplayId, String visualStyle, String aspectRatio) {
        User user = authService.getCurrentUser();
        ScreenplayProject screenplay = screenplayRepository.findById(screenplayId)
                .orElseThrow(() -> new ResourceNotFoundException("Screenplay not found with id: " + screenplayId));

        // Create or reuse visualization session
        StoryVisualization viz = visualizationRepository.findTopByScreenplayIdOrderByUpdatedAtDesc(screenplayId)
                .orElse(new StoryVisualization());

        viz.setScreenplay(screenplay);
        viz.setUser(user);
        viz.setTitle(screenplay.getTitle() + " - Visual Experience");
        viz.setVisualStyle(visualStyle != null ? visualStyle : "Cinematic Photorealism");
        viz.setAspectRatio(aspectRatio != null ? aspectRatio : "16:9");
        viz.setStatus("ANALYZED");
        viz = visualizationRepository.save(viz);

        // Clear previous runs for this visualization session to ensure clean regeneration
        imageRepository.deleteByVisualizationId(viz.getId());
        sceneRepository.deleteByVisualizationId(viz.getId());
        characterReferenceRepository.deleteByVisualizationId(viz.getId());

        // Phase 1: Deep Story Analysis
        StoryAnalysisService.AnalysisResult analysis = analysisService.analyzeCompleteStory(
                screenplay.getTitle(),
                screenplay.getSynopsis(),
                screenplay.getStoryConcept(),
                screenplay.getGenre()
        );

        viz.setAnalysisProvenance(analysis.provenance);
        viz.setStoryLogline(analysis.logline);
        viz.setStoryTheme(analysis.theme);
        viz.setStorySynopsis(analysis.synopsis);

        // Phase 2: Create Character References
        List<StoryCharacterReference> savedChars = new ArrayList<>();
        int charSeed = 100;
        for (StoryAnalysisService.ExtractedCharacter ec : analysis.characters) {
            StoryCharacterReference cr = new StoryCharacterReference();
            cr.setVisualization(viz);
            cr.setCharacterName(ec.name);
            cr.setRoleType(ec.roleType);
            cr.setApproxAge(ec.approxAge);
            cr.setGenderPresentation(ec.genderPresentation);
            cr.setFacialFeatures(ec.facialFeatures);
            cr.setHairstyle(ec.hairstyle);
            cr.setHairColor(ec.hairColor);
            cr.setClothingContinuity(ec.clothingContinuity);
            cr.setDistinctiveProps(ec.distinctiveProps);
            cr.setIsApproved(false);

            // Generate character reference visual & store permanently
            AIImageGenerationService.ImageGenResult gen = imageGenService.generateSceneVisual(
                    "Close-up portrait", "Eye Level",
                    "Character reference portrait: " + ec.name + ", " + ec.approxAge + ", " + ec.facialFeatures + ", wearing " + ec.clothingContinuity,
                    "Studio Character Reference", ec.name, viz.getVisualStyle(), charSeed++
            );

            try {
                ImageStorageService.StoredFile sf;
                if (gen.imageBytes != null) {
                    sf = storageService.savePermanentImage(user.getId(), viz.getId(), "char_" + ec.name.replaceAll("\\s+", "_"), gen.imageBytes, gen.format);
                } else {
                    sf = storageService.saveSvgIllustration(user.getId(), viz.getId(), "char_" + ec.name.replaceAll("\\s+", "_"), gen.svgContent);
                }
                cr.setStoragePath(sf.storagePath);
                cr.setReferenceImageUrl(sf.servingUrl);
            } catch (IOException io) {
                cr.setReferenceImageUrl("data:image/svg+xml;utf8," + java.net.URLEncoder.encode(gen.svgContent, java.nio.charset.StandardCharsets.UTF_8).replace("+", "%20"));
            }

            cr.setImageProvenance(gen.provenance);
            savedChars.add(characterReferenceRepository.save(cr));
        }
        viz.setTotalCharacters(savedChars.size());

        // Phase 3: Create Chronological Visualization Scenes
        List<VisualizationScene> savedScenes = new ArrayList<>();
        for (StoryAnalysisService.ExtractedScene es : analysis.scenes) {
            VisualizationScene vs = new VisualizationScene();
            vs.setVisualization(viz);
            vs.setSceneNumber(es.sceneNumber);
            vs.setSceneHeading(es.sceneHeading);
            vs.setLocationName(es.locationName);
            vs.setTimeOfDay(es.timeOfDay);
            vs.setCharactersPresent(es.charactersPresent);
            vs.setDramaticAction(es.dramaticAction);
            vs.setEmotionalTone(es.emotionalTone);
            vs.setSuggestedCameraAngle(es.suggestedCameraAngle);
            vs.setCameraMovement(es.cameraMovement);
            vs.setNarrativeTransition(es.narrativeTransition);
            vs.setChronologyOrder(es.sceneNumber);
            vs.setIsFlashback(es.isFlashback);
            savedScenes.add(sceneRepository.save(vs));
        }
        viz.setTotalScenes(savedScenes.size());

        // Phase 4: Generate Scene Images & Store Permanently
        List<VisualizationImage> savedImages = new ArrayList<>();
        int frameCounter = 1;
        String leadCharName = (!savedChars.isEmpty()) ? savedChars.get(0).getCharacterName() : "Protagonist";

        for (VisualizationScene sc : savedScenes) {
            VisualizationImage img = new VisualizationImage();
            img.setVisualization(viz);
            img.setScene(sc);
            img.setFrameOrder(frameCounter++);
            img.setCameraShotType(sc.getSuggestedCameraAngle());
            img.setGenerationPrompt(sc.getDramaticAction() + " at " + sc.getLocationName() + " with " + sc.getCharactersPresent());

            AIImageGenerationService.ImageGenResult gen = imageGenService.generateSceneVisual(
                    sc.getSuggestedCameraAngle(), sc.getSuggestedCameraAngle(),
                    img.getGenerationPrompt(), sc.getLocationName(), leadCharName, viz.getVisualStyle(), frameCounter
            );

            try {
                ImageStorageService.StoredFile sf;
                if (gen.imageBytes != null) {
                    sf = storageService.savePermanentImage(user.getId(), viz.getId(), "scene_" + sc.getSceneNumber(), gen.imageBytes, gen.format);
                } else {
                    sf = storageService.saveSvgIllustration(user.getId(), viz.getId(), "scene_" + sc.getSceneNumber(), gen.svgContent);
                }
                img.setStoragePath(sf.storagePath);
                img.setImageUrl(sf.servingUrl);
            } catch (IOException io) {
                img.setImageUrl("data:image/svg+xml;utf8," + java.net.URLEncoder.encode(gen.svgContent, java.nio.charset.StandardCharsets.UTF_8).replace("+", "%20"));
            }

            img.setImageProvenance(gen.provenance);
            img.setGenerationStatus("COMPLETED");
            img.setErrorMessage(gen.errorMessage);
            savedImages.add(imageRepository.save(img));
        }
        viz.setTotalImages(savedImages.size());
        viz.setStatus("COMPLETED");
        visualizationRepository.save(viz);

        // Phase 5: Generate 5 Story Charts
        FullVisualizationResponse resp = new FullVisualizationResponse();
        resp.visualization = viz;
        resp.characters = savedChars;
        resp.scenes = savedScenes;
        resp.images = savedImages;
        resp.quotaNotice = analysis.quotaNotice;

        resp.charts.put("structureChart", chartGenerationService.generateStoryStructureChart(viz.getTitle(), analysis.structureBeats));
        resp.charts.put("relationshipMap", chartGenerationService.generateCharacterRelationshipMap(analysis.relationships));
        resp.charts.put("storyTimeline", chartGenerationService.generateStoryTimeline(analysis.timelineEvents));

        List<Map<String, String>> sceneMaps = new ArrayList<>();
        for (VisualizationScene vs : savedScenes) {
            Map<String, String> m = new HashMap<>();
            m.put("heading", vs.getSceneHeading());
            m.put("transition", vs.getNarrativeTransition());
            sceneMaps.add(m);
        }
        resp.charts.put("transitionFlowchart", chartGenerationService.generateSceneTransitionFlowchart(sceneMaps));
        resp.charts.put("emotionalArc", chartGenerationService.generateEmotionalArcGraph(null));

        return resp;
    }

    public FullVisualizationResponse getVisualizationDetail(Long visualizationId) {
        StoryVisualization viz = visualizationRepository.findById(visualizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Visualization not found: " + visualizationId));

        FullVisualizationResponse resp = new FullVisualizationResponse();
        resp.visualization = viz;
        resp.characters = characterReferenceRepository.findByVisualizationIdOrderByIdAsc(viz.getId());
        resp.scenes = sceneRepository.findByVisualizationIdOrderBySceneNumberAsc(viz.getId());
        resp.images = imageRepository.findByVisualizationIdOrderByFrameOrderAsc(viz.getId());

        // Generate Charts dynamically
        List<String> beats = List.of("Setup", "Inciting Incident", "Midpoint Shift", "Climax", "Resolution");
        resp.charts.put("structureChart", chartGenerationService.generateStoryStructureChart(viz.getTitle(), beats));
        resp.charts.put("relationshipMap", chartGenerationService.generateCharacterRelationshipMap(null));
        resp.charts.put("storyTimeline", chartGenerationService.generateStoryTimeline(null));
        resp.charts.put("transitionFlowchart", chartGenerationService.generateSceneTransitionFlowchart(null));
        resp.charts.put("emotionalArc", chartGenerationService.generateEmotionalArcGraph(null));

        return resp;
    }

    @Transactional
    public StoryCharacterReference approveCharacter(Long characterId) {
        StoryCharacterReference cr = characterReferenceRepository.findById(characterId)
                .orElseThrow(() -> new ResourceNotFoundException("Character reference not found: " + characterId));
        cr.setIsApproved(true);
        return characterReferenceRepository.save(cr);
    }

    @Transactional
    public VisualizationImage regenerateSingleImage(Long imageId) {
        VisualizationImage img = imageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Visualization image not found: " + imageId));

        VisualizationScene sc = img.getScene();
        StoryVisualization viz = img.getVisualization();
        int newSeed = (int) (System.currentTimeMillis() % 10000);

        AIImageGenerationService.ImageGenResult gen = imageGenService.generateSceneVisual(
                img.getCameraShotType(), img.getCameraShotType(),
                img.getGenerationPrompt(), sc.getLocationName(), "Protagonist", viz.getVisualStyle(), newSeed
        );

        try {
            ImageStorageService.StoredFile sf;
            if (gen.imageBytes != null) {
                sf = storageService.savePermanentImage(viz.getUser().getId(), viz.getId(), "scene_regen_" + sc.getSceneNumber(), gen.imageBytes, gen.format);
            } else {
                sf = storageService.saveSvgIllustration(viz.getUser().getId(), viz.getId(), "scene_regen_" + sc.getSceneNumber(), gen.svgContent);
            }
            img.setStoragePath(sf.storagePath);
            img.setImageUrl(sf.servingUrl);
        } catch (IOException io) {
            img.setImageUrl("data:image/svg+xml;utf8," + java.net.URLEncoder.encode(gen.svgContent, java.nio.charset.StandardCharsets.UTF_8).replace("+", "%20"));
        }

        img.setImageProvenance(gen.provenance);
        img.setVersionNumber(img.getVersionNumber() + 1);
        img.setErrorMessage(gen.errorMessage);
        return imageRepository.save(img);
    }
}
