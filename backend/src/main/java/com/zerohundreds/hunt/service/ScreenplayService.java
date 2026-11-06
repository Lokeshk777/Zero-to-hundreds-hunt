package com.zerohundreds.hunt.service;

import com.zerohundreds.hunt.dto.ScreenplayDtos.*;
import com.zerohundreds.hunt.entity.*;
import com.zerohundreds.hunt.exception.ResourceNotFoundException;
import com.zerohundreds.hunt.repository.*;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ScreenplayService {

    private final ScreenplayProjectRepository screenplayRepository;
    private final StoryStructureRepository structureRepository;
    private final CharacterProfileRepository characterRepository;
    private final SceneRepository sceneRepository;
    private final ScreenplayDraftRepository draftRepository;
    private final ProductionProjectRepository productionRepository;
    private final AiConversationRepository aiConversationRepository;
    private final CreativeIdeaRepository creativeIdeaRepository;
    private final AuthService authService;
    private final EntityManager entityManager;

    public ScreenplayService(ScreenplayProjectRepository screenplayRepository,
                             StoryStructureRepository structureRepository,
                             CharacterProfileRepository characterRepository,
                             SceneRepository sceneRepository,
                             ScreenplayDraftRepository draftRepository,
                             ProductionProjectRepository productionRepository,
                             AiConversationRepository aiConversationRepository,
                             CreativeIdeaRepository creativeIdeaRepository,
                             AuthService authService,
                             EntityManager entityManager) {
        this.screenplayRepository = screenplayRepository;
        this.structureRepository = structureRepository;
        this.characterRepository = characterRepository;
        this.sceneRepository = sceneRepository;
        this.draftRepository = draftRepository;
        this.productionRepository = productionRepository;
        this.aiConversationRepository = aiConversationRepository;
        this.creativeIdeaRepository = creativeIdeaRepository;
        this.authService = authService;
        this.entityManager = entityManager;
    }

    // --- Screenplays ---

    public List<ScreenplayProject> getAllScreenplays() {
        User user = authService.getCurrentUser();
        return screenplayRepository.findByUserOrderByUpdatedAtDesc(user);
    }

    public ScreenplayProject getScreenplay(Long id) {
        User user = authService.getCurrentUser();
        return screenplayRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Screenplay project not found with ID: " + id));
    }

    @Transactional
    public ScreenplayProject createScreenplay(ScreenplayRequest request) {
        User user = authService.getCurrentUser();
        ScreenplayProject project = new ScreenplayProject();
        project.setUser(user);
        project.setTitle(request.getTitle().trim());
        project.setGenre(request.getGenre());
        project.setStoryConcept(request.getStoryConcept());
        project.setLogline(request.getLogline());
        project.setSynopsis(request.getSynopsis());
        project.setTheme(request.getTheme());
        project.setCentralConflict(request.getCentralConflict());
        project.setStructureType(request.getStructureType() != null ? request.getStructureType() : "THREE_ACT");
        project.setStatus(request.getStatus() != null ? request.getStatus() : "DRAFT");

        ScreenplayProject saved = screenplayRepository.save(project);

        // Populate default structure beats based on structureType
        initDefaultStructure(saved, saved.getStructureType());

        // Create empty draft
        ScreenplayDraft draft = new ScreenplayDraft();
        draft.setScreenplay(saved);
        draft.setUser(user);
        draft.setRawContent("FADE IN:\n\nEXT. CITY STREET - DAY\n\nA bustling city wakes under a golden sunrise.\n\nFADE OUT.");
        draftRepository.save(draft);

        return saved;
    }

    @Transactional
    public ScreenplayProject updateScreenplay(Long id, ScreenplayRequest request) {
        ScreenplayProject project = getScreenplay(id);
        project.setTitle(request.getTitle().trim());
        project.setGenre(request.getGenre());
        project.setStoryConcept(request.getStoryConcept());
        project.setLogline(request.getLogline());
        project.setSynopsis(request.getSynopsis());
        project.setTheme(request.getTheme());
        project.setCentralConflict(request.getCentralConflict());
        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }
        if (request.getStructureType() != null && !request.getStructureType().equals(project.getStructureType())) {
            project.setStructureType(request.getStructureType());
            initDefaultStructure(project, request.getStructureType());
        }
        return screenplayRepository.save(project);
    }

    @Transactional
    public void deleteScreenplay(Long id) {
        ScreenplayProject project = getScreenplay(id);

        // 1. Unlink any Production Projects pointing to this screenplay
        List<ProductionProject> productions = productionRepository.findByUserOrderByUpdatedAtDesc(project.getUser());
        for (ProductionProject p : productions) {
            if (p.getScreenplay() != null && p.getScreenplay().getId().equals(project.getId())) {
                p.setScreenplay(null);
                productionRepository.save(p);
            }
        }

        // 2. Unlink any AI Conversations pointing to this screenplay
        List<AiConversation> convs = aiConversationRepository.findByUserOrderByUpdatedAtDesc(project.getUser());
        for (AiConversation c : convs) {
            if (c.getScreenplay() != null && c.getScreenplay().getId().equals(project.getId())) {
                c.setScreenplay(null);
                aiConversationRepository.save(c);
            }
        }

        // 3. Unlink any Creative Vault ideas converted into this screenplay
        List<CreativeIdea> ideas = creativeIdeaRepository.findByUserOrderByUpdatedAtDesc(project.getUser());
        for (CreativeIdea idea : ideas) {
            if (idea.getConvertedToScreenplay() != null && idea.getConvertedToScreenplay().getId().equals(project.getId())) {
                idea.setConvertedToScreenplay(null);
                creativeIdeaRepository.save(idea);
            }
        }

        // 4. Force immediate deletion of all child entities before deleting parent
        draftRepository.deleteByScreenplay(project);
        sceneRepository.deleteByScreenplay(project);
        characterRepository.deleteByScreenplay(project);
        structureRepository.deleteByScreenplay(project);

        entityManager.flush();

        // 5. Delete screenplay project
        screenplayRepository.delete(project);
        entityManager.flush();
    }

    // --- Story Structure ---

    public List<StoryStructure> getStructureStages(Long screenplayId) {
        ScreenplayProject project = getScreenplay(screenplayId);
        return structureRepository.findByScreenplayOrderByStageOrderAsc(project);
    }

    @Transactional
    public StoryStructure updateStructureStage(Long screenplayId, Long stageId, StructureStageRequest request) {
        ScreenplayProject project = getScreenplay(screenplayId);
        StoryStructure stage = structureRepository.findById(stageId)
                .orElseThrow(() -> new ResourceNotFoundException("Structure stage not found"));
        if (!stage.getScreenplay().getId().equals(project.getId())) {
            throw new ResourceNotFoundException("Structure stage does not belong to this project");
        }
        if (request.getNotes() != null) stage.setNotes(request.getNotes());
        if (request.getDescription() != null) stage.setDescription(request.getDescription());
        return structureRepository.save(stage);
    }

    private void initDefaultStructure(ScreenplayProject screenplay, String structureType) {
        structureRepository.deleteByScreenplay(screenplay);

        if ("FIVE_ACT".equalsIgnoreCase(structureType)) {
            addStage(screenplay, "Five-Act Structure", "Act I: Exposition / Prologue", 1, "Introduces characters, setting, tone, and the initial status quo before disruption.");
            addStage(screenplay, "Five-Act Structure", "Act II: Rising Action", 2, "Conflicts escalate, secondary characters are entangled, obstacles compound.");
            addStage(screenplay, "Five-Act Structure", "Act III: Climax", 3, "The peak of narrative conflict where fortunes reverse irrevocably.");
            addStage(screenplay, "Five-Act Structure", "Act IV: Falling Action", 4, "The aftermath of the climax unfolds, secrets unravel, leading toward closure.");
            addStage(screenplay, "Five-Act Structure", "Act V: Resolution / Catastrophe", 5, "Final equilibrium or tragedy established, thematic catharsis delivered.");
        } else if ("HEROS_JOURNEY".equalsIgnoreCase(structureType)) {
            addStage(screenplay, "Hero's Journey", "1. Ordinary World", 1, "The protagonist's mundane existence prior to adventure.");
            addStage(screenplay, "Hero's Journey", "2. Call to Adventure", 2, "A challenge or disruption invites the hero outside their comfort zone.");
            addStage(screenplay, "Hero's Journey", "3. Refusal of the Call & Mentor", 3, "Hesitation overcome by a seasoned mentor offering wisdom or tools.");
            addStage(screenplay, "Hero's Journey", "4. Crossing the First Threshold", 4, "Committing wholeheartedly to entering the unfamiliar Special World.");
            addStage(screenplay, "Hero's Journey", "5. Tests, Allies, and Enemies", 5, "Navigating the rules of the new world and forming crucial bonds.");
            addStage(screenplay, "Hero's Journey", "6. The Ordeal & Reward", 6, "Facing the deepest fear in mortal danger, emerging reborn with the elixir.");
            addStage(screenplay, "Hero's Journey", "7. Return with Elixir", 7, "Returning to restore balance in the ordinary world transformed.");
        } else if ("SAVE_THE_CAT".equalsIgnoreCase(structureType)) {
            addStage(screenplay, "Save the Cat", "Opening Image & Theme Stated", 1, "A snapshot of the hero's flawed starting world and the philosophical premise.");
            addStage(screenplay, "Save the Cat", "Set-up & Catalyst (Beat 12)", 2, "Life before disruption, followed by an inciting incident changing everything.");
            addStage(screenplay, "Save the Cat", "Debate & Break into Two", 3, "Resisting change before stepping into Act 2 with a proactive plan.");
            addStage(screenplay, "Save the Cat", "B Story & Fun and Games", 4, "The promise of the premise, exploration of themes through relationship.");
            addStage(screenplay, "Save the Cat", "Midpoint & Bad Guys Close In", 5, "False victory/defeat raising stakes; internal/external pressures surge.");
            addStage(screenplay, "Save the Cat", "All is Lost & Dark Night of the Soul", 6, "Rock bottom, a whiff of death, followed by profound introspection.");
            addStage(screenplay, "Save the Cat", "Break into Three & Finale", 7, "The synthesis of lessons learned executes a brilliant five-point plan to victory.");
        } else if ("NON_LINEAR".equalsIgnoreCase(structureType)) {
            addStage(screenplay, "Non-Linear Structure", "Anchor Frame (Present)", 1, "The central timeframe grounding the viewer or narrative revelation.");
            addStage(screenplay, "Non-Linear Structure", "Memory Fragment / Flashback Alpha", 2, "Early pivotal event contextualizing current motivations or trauma.");
            addStage(screenplay, "Non-Linear Structure", "Converging Timeline Beta", 3, "Alternate perspective or chronological counterpoint creating irony.");
            addStage(screenplay, "Non-Linear Structure", "Thematic Nexus", 4, "The point where disconnected chronological threads illuminate truth.");
            addStage(screenplay, "Non-Linear Structure", "Echo Resolution", 5, "Final narrative reveal that recontextualizes the opening moment.");
        } else {
            // Three-Act Structure (Default)
            addStage(screenplay, "Three-Act Structure", "Act I: Setup & Inciting Incident", 1, "Establishes ordinary world, protagonist flaw, inciting incident (page 10-12), and Plot Point 1.");
            addStage(screenplay, "Three-Act Structure", "Act II-A: Rising Conflict & Midpoint", 2, "Protagonist pursues goal against rising obstacles until the dramatic Midpoint reversal.");
            addStage(screenplay, "Three-Act Structure", "Act II-B: Crisis & All Hope Lost", 3, "Stakes soar, antagonist strikes back, leading to the lowest point (Plot Point 2).");
            addStage(screenplay, "Three-Act Structure", "Act III: Climax & Resolution", 4, "Final showdown pitting hero's growth against core obstacle, followed by new equilibrium.");
        }
    }

    private void addStage(ScreenplayProject screenplay, String structureName, String stageName, int order, String desc) {
        StoryStructure s = new StoryStructure();
        s.setScreenplay(screenplay);
        s.setStructureName(structureName);
        s.setStageName(stageName);
        s.setStageOrder(order);
        s.setDescription(desc);
        s.setNotes("");
        structureRepository.save(s);
    }

    // --- Characters ---

    public List<CharacterProfile> getCharacters(Long screenplayId) {
        ScreenplayProject project = getScreenplay(screenplayId);
        User user = authService.getCurrentUser();
        return characterRepository.findByScreenplayAndUserOrderByCreatedAtAsc(project, user);
    }

    @Transactional
    public CharacterProfile createCharacter(Long screenplayId, CharacterRequest request) {
        ScreenplayProject project = getScreenplay(screenplayId);
        User user = authService.getCurrentUser();

        CharacterProfile character = new CharacterProfile();
        character.setScreenplay(project);
        character.setUser(user);
        character.setName(request.getName().trim());
        character.setRoleType(request.getRoleType() != null ? request.getRoleType() : "MAIN");
        character.setAge(request.getAge());
        character.setBackground(request.getBackground());
        character.setPersonality(request.getPersonality());
        character.setMotivation(request.getMotivation());
        character.setGoals(request.getGoals());
        character.setStrengths(request.getStrengths());
        character.setWeaknesses(request.getWeaknesses());
        character.setInternalConflict(request.getInternalConflict());
        character.setExternalConflict(request.getExternalConflict());
        character.setCharacterArc(request.getCharacterArc());

        return characterRepository.save(character);
    }

    @Transactional
    public CharacterProfile updateCharacter(Long screenplayId, Long charId, CharacterRequest request) {
        ScreenplayProject project = getScreenplay(screenplayId);
        User user = authService.getCurrentUser();

        CharacterProfile character = characterRepository.findByIdAndUser(charId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Character profile not found"));
        if (!character.getScreenplay().getId().equals(project.getId())) {
            throw new ResourceNotFoundException("Character does not belong to this screenplay");
        }

        character.setName(request.getName().trim());
        character.setRoleType(request.getRoleType());
        character.setAge(request.getAge());
        character.setBackground(request.getBackground());
        character.setPersonality(request.getPersonality());
        character.setMotivation(request.getMotivation());
        character.setGoals(request.getGoals());
        character.setStrengths(request.getStrengths());
        character.setWeaknesses(request.getWeaknesses());
        character.setInternalConflict(request.getInternalConflict());
        character.setExternalConflict(request.getExternalConflict());
        character.setCharacterArc(request.getCharacterArc());

        return characterRepository.save(character);
    }

    @Transactional
    public void deleteCharacter(Long screenplayId, Long charId) {
        ScreenplayProject project = getScreenplay(screenplayId);
        User user = authService.getCurrentUser();
        CharacterProfile character = characterRepository.findByIdAndUser(charId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Character profile not found"));
        if (!character.getScreenplay().getId().equals(project.getId())) {
            throw new ResourceNotFoundException("Character does not belong to this screenplay");
        }
        characterRepository.delete(character);
    }

    // --- Scenes ---

    public List<Scene> getScenes(Long screenplayId) {
        ScreenplayProject project = getScreenplay(screenplayId);
        User user = authService.getCurrentUser();
        return sceneRepository.findByScreenplayAndUserOrderBySceneNumberAsc(project, user);
    }

    @Transactional
    public Scene createScene(Long screenplayId, SceneRequest request) {
        ScreenplayProject project = getScreenplay(screenplayId);
        User user = authService.getCurrentUser();

        Scene scene = new Scene();
        scene.setScreenplay(project);
        scene.setUser(user);

        long count = sceneRepository.countByScreenplayAndUser(project, user);
        scene.setSceneNumber(request.getSceneNumber() != null ? request.getSceneNumber() : (int) (count + 1));
        scene.setHeadingType(request.getHeadingType() != null ? request.getHeadingType() : "INT.");
        scene.setLocation(request.getLocation().trim());
        scene.setTimeOfDay(request.getTimeOfDay() != null ? request.getTimeOfDay() : "DAY");
        scene.setSceneHeading(scene.getHeadingType() + " " + scene.getLocation() + " - " + scene.getTimeOfDay());
        scene.setDescription(request.getDescription());
        scene.setCharacterAppearances(request.getCharacterAppearances());
        scene.setActions(request.getActions());
        scene.setDialogueContent(request.getDialogueContent());
        scene.setEmotionalPurpose(request.getEmotionalPurpose());
        scene.setSceneObjective(request.getSceneObjective());
        scene.setPropsNeeded(request.getPropsNeeded());
        scene.setEquipmentNeeded(request.getEquipmentNeeded());
        scene.setSoundNotes(request.getSoundNotes());

        return sceneRepository.save(scene);
    }

    @Transactional
    public Scene updateScene(Long screenplayId, Long sceneId, SceneRequest request) {
        ScreenplayProject project = getScreenplay(screenplayId);
        User user = authService.getCurrentUser();

        Scene scene = sceneRepository.findByIdAndUser(sceneId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Scene not found"));
        if (!scene.getScreenplay().getId().equals(project.getId())) {
            throw new ResourceNotFoundException("Scene does not belong to this screenplay");
        }

        if (request.getSceneNumber() != null) scene.setSceneNumber(request.getSceneNumber());
        if (request.getHeadingType() != null) scene.setHeadingType(request.getHeadingType());
        if (request.getLocation() != null) scene.setLocation(request.getLocation().trim());
        if (request.getTimeOfDay() != null) scene.setTimeOfDay(request.getTimeOfDay());
        scene.setSceneHeading(scene.getHeadingType() + " " + scene.getLocation() + " - " + scene.getTimeOfDay());
        scene.setDescription(request.getDescription());
        scene.setCharacterAppearances(request.getCharacterAppearances());
        scene.setActions(request.getActions());
        scene.setDialogueContent(request.getDialogueContent());
        scene.setEmotionalPurpose(request.getEmotionalPurpose());
        scene.setSceneObjective(request.getSceneObjective());
        scene.setPropsNeeded(request.getPropsNeeded());
        scene.setEquipmentNeeded(request.getEquipmentNeeded());
        scene.setSoundNotes(request.getSoundNotes());

        return sceneRepository.save(scene);
    }

    @Transactional
    public void deleteScene(Long screenplayId, Long sceneId) {
        ScreenplayProject project = getScreenplay(screenplayId);
        User user = authService.getCurrentUser();
        Scene scene = sceneRepository.findByIdAndUser(sceneId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Scene not found"));
        if (!scene.getScreenplay().getId().equals(project.getId())) {
            throw new ResourceNotFoundException("Scene does not belong to this screenplay");
        }
        sceneRepository.delete(scene);
    }

    // --- Draft Editor ---

    public ScreenplayDraft getDraft(Long screenplayId) {
        ScreenplayProject project = getScreenplay(screenplayId);
        User user = authService.getCurrentUser();
        return draftRepository.findByScreenplayAndUser(project, user)
                .orElseGet(() -> {
                    ScreenplayDraft newDraft = new ScreenplayDraft();
                    newDraft.setScreenplay(project);
                    newDraft.setUser(user);
                    newDraft.setRawContent("FADE IN:\n\nEXT. LOCATION - DAY\n\nBegin your script here.");
                    return draftRepository.save(newDraft);
                });
    }

    @Transactional
    public ScreenplayDraft saveDraft(Long screenplayId, DraftSaveRequest request) {
        ScreenplayProject project = getScreenplay(screenplayId);
        User user = authService.getCurrentUser();

        ScreenplayDraft draft = draftRepository.findByScreenplayAndUser(project, user)
                .orElseGet(() -> {
                    ScreenplayDraft d = new ScreenplayDraft();
                    d.setScreenplay(project);
                    d.setUser(user);
                    return d;
                });

        draft.setRawContent(request.getRawContent());
        if (request.getPageCount() != null && request.getPageCount() > 0) {
            draft.setPageCount(request.getPageCount());
        }
        return draftRepository.save(draft);
    }
}
