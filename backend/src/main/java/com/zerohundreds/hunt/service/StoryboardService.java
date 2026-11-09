package com.zerohundreds.hunt.service;

import com.zerohundreds.hunt.dto.StoryboardDtos.*;
import com.zerohundreds.hunt.entity.*;
import com.zerohundreds.hunt.exception.ResourceNotFoundException;
import com.zerohundreds.hunt.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StoryboardService {

    private final StoryboardRepository storyboardRepository;
    private final StoryboardFrameRepository frameRepository;
    private final ScreenplayProjectRepository screenplayRepository;
    private final SceneRepository sceneRepository;
    private final AIStoryboardAnalysisService analysisService;
    private final StoryboardImageGenerationService imageGenerationService;
    private final AuthService authService;

    public StoryboardService(
            StoryboardRepository storyboardRepository,
            StoryboardFrameRepository frameRepository,
            ScreenplayProjectRepository screenplayRepository,
            SceneRepository sceneRepository,
            AIStoryboardAnalysisService analysisService,
            StoryboardImageGenerationService imageGenerationService,
            AuthService authService) {
        this.storyboardRepository = storyboardRepository;
        this.frameRepository = frameRepository;
        this.screenplayRepository = screenplayRepository;
        this.sceneRepository = sceneRepository;
        this.analysisService = analysisService;
        this.imageGenerationService = imageGenerationService;
        this.authService = authService;
    }

    public List<Storyboard> getAllStoryboards() {
        User user = authService.getCurrentUser();
        return storyboardRepository.findByUserIdOrderByUpdatedAtDesc(user.getId());
    }

    public Storyboard getStoryboard(Long id) {
        return storyboardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Storyboard not found with id: " + id));
    }

    public List<Storyboard> getStoryboardsByScreenplay(Long screenplayId) {
        return storyboardRepository.findByScreenplayIdOrderByVersionNumberDesc(screenplayId);
    }

    public List<StoryboardFrame> getStoryboardFrames(Long storyboardId) {
        return frameRepository.findByStoryboardIdOrderByDisplayOrderAsc(storyboardId);
    }

    @Transactional
    public Storyboard generateStoryboard(GenerateStoryboardRequest request) {
        User user = authService.getCurrentUser();
        ScreenplayProject screenplay = screenplayRepository.findById(request.getScreenplayId())
                .orElseThrow(() -> new ResourceNotFoundException("Screenplay not found with id: " + request.getScreenplayId()));

        List<Storyboard> existing = storyboardRepository.findByScreenplayIdOrderByVersionNumberDesc(screenplay.getId());
        int newVersion = 1;
        if (!existing.isEmpty() && Boolean.TRUE.equals(request.getCreateNewVersion())) {
            newVersion = existing.get(0).getVersionNumber() + 1;
        }

        Storyboard storyboard;
        if (existing.isEmpty() || Boolean.TRUE.equals(request.getCreateNewVersion())) {
            storyboard = new Storyboard();
            storyboard.setScreenplay(screenplay);
            storyboard.setUser(user);
            storyboard.setTitle(screenplay.getTitle() + " - Storyboard");
            storyboard.setVersionNumber(newVersion);
            storyboard.setVersionName("Revision " + newVersion);
            storyboard.setStatus("READY");
            storyboard = storyboardRepository.save(storyboard);
        } else {
            storyboard = existing.get(0);
            frameRepository.deleteByStoryboardId(storyboard.getId());
        }

        List<Scene> scenes = sceneRepository.findByScreenplayIdOrderBySceneNumberAsc(screenplay.getId());
        List<AIStoryboardAnalysisService.AnalyzedShot> analyzedShots = analysisService.analyzeScreenplayScenes(screenplay, scenes);

        int order = 1;
        for (AIStoryboardAnalysisService.AnalyzedShot shot : analyzedShots) {
            StoryboardFrame frame = new StoryboardFrame();
            frame.setStoryboard(storyboard);
            frame.setSceneNumber(shot.sceneNumber);
            frame.setSceneHeading(shot.sceneHeading);
            frame.setFrameNumber(shot.frameNumber);
            frame.setShotType(shot.shotType);
            frame.setCameraAngle(shot.cameraAngle);
            frame.setCameraMovement(shot.cameraMovement);
            frame.setLighting(shot.lighting);
            frame.setCharacterDetails(shot.characterDetails);
            frame.setShotDescription(shot.shotDescription);
            frame.setDialogueReference(shot.dialogueReference);
            frame.setImageUrl(shot.imageUrl);
            frame.setDisplayOrder(order++);
            frameRepository.save(frame);
        }

        storyboard.setTotalScenes(scenes.isEmpty() ? 1 : scenes.size());
        storyboard.setTotalFrames(analyzedShots.size());
        return storyboardRepository.save(storyboard);
    }

    @Transactional
    public StoryboardFrame updateFrame(Long frameId, UpdateFrameRequest req) {
        StoryboardFrame frame = frameRepository.findById(frameId)
                .orElseThrow(() -> new ResourceNotFoundException("Frame not found with id: " + frameId));

        if (req.getShotType() != null) frame.setShotType(req.getShotType());
        if (req.getCameraAngle() != null) frame.setCameraAngle(req.getCameraAngle());
        if (req.getCameraMovement() != null) frame.setCameraMovement(req.getCameraMovement());
        if (req.getLighting() != null) frame.setLighting(req.getLighting());
        if (req.getShotDescription() != null) frame.setShotDescription(req.getShotDescription());
        if (req.getDialogueReference() != null) frame.setDialogueReference(req.getDialogueReference());
        if (req.getDirectorsNotes() != null) frame.setDirectorsNotes(req.getDirectorsNotes());
        if (req.getImageUrl() != null) frame.setImageUrl(req.getImageUrl());

        return frameRepository.save(frame);
    }

    @Transactional
    public StoryboardFrame regenerateSingleFrame(Long frameId) {
        StoryboardFrame frame = frameRepository.findById(frameId)
                .orElseThrow(() -> new ResourceNotFoundException("Frame not found with id: " + frameId));

        // Generate a new visual sketch variation for this particular shot
        int randomSeed = (int) (System.currentTimeMillis() % 10000);
        String newSvg = imageGenerationService.generateSketchSvg(
                frame.getShotType(),
                frame.getCameraAngle(),
                frame.getShotDescription(),
                frame.getSceneHeading(),
                randomSeed
        );

        frame.setImageUrl(newSvg);
        return frameRepository.save(frame);
    }

    @Transactional
    public void deleteFrame(Long frameId) {
        StoryboardFrame frame = frameRepository.findById(frameId)
                .orElseThrow(() -> new ResourceNotFoundException("Frame not found with id: " + frameId));
        Storyboard sb = frame.getStoryboard();
        frameRepository.delete(frame);
        if (sb != null) {
            sb.setTotalFrames(Math.max(0, sb.getTotalFrames() - 1));
            storyboardRepository.save(sb);
        }
    }

    @Transactional
    public void deleteStoryboard(Long storyboardId) {
        frameRepository.deleteByStoryboardId(storyboardId);
        storyboardRepository.deleteById(storyboardId);
    }
}
