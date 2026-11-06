package com.zerohundreds.hunt.controller;

import com.zerohundreds.hunt.dto.ApiResponse;
import com.zerohundreds.hunt.dto.ScreenplayDtos.*;
import com.zerohundreds.hunt.entity.*;
import com.zerohundreds.hunt.service.ScreenplayService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/screenplays")
public class ScreenplayController {

    private final ScreenplayService screenplayService;

    public ScreenplayController(ScreenplayService screenplayService) {
        this.screenplayService = screenplayService;
    }

    // Projects CRUD
    @GetMapping
    public ResponseEntity<ApiResponse<List<ScreenplayProject>>> getAllScreenplays() {
        return ResponseEntity.ok(ApiResponse.ok("Screenplays retrieved", screenplayService.getAllScreenplays()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ScreenplayProject>> getScreenplay(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Screenplay retrieved", screenplayService.getScreenplay(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ScreenplayProject>> createScreenplay(@Valid @RequestBody ScreenplayRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Screenplay project created", screenplayService.createScreenplay(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ScreenplayProject>> updateScreenplay(@PathVariable Long id, @Valid @RequestBody ScreenplayRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Screenplay project updated", screenplayService.updateScreenplay(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteScreenplay(@PathVariable Long id) {
        screenplayService.deleteScreenplay(id);
        return ResponseEntity.ok(ApiResponse.ok("Screenplay project deleted successfully"));
    }

    // Structure
    @GetMapping("/{id}/structures")
    public ResponseEntity<ApiResponse<List<StoryStructure>>> getStructures(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Structure beats retrieved", screenplayService.getStructureStages(id)));
    }

    @PutMapping("/{id}/structures/{stageId}")
    public ResponseEntity<ApiResponse<StoryStructure>> updateStructureStage(
            @PathVariable Long id,
            @PathVariable Long stageId,
            @RequestBody StructureStageRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Structure beat updated", screenplayService.updateStructureStage(id, stageId, request)));
    }

    // Characters
    @GetMapping("/{id}/characters")
    public ResponseEntity<ApiResponse<List<CharacterProfile>>> getCharacters(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Characters retrieved", screenplayService.getCharacters(id)));
    }

    @PostMapping("/{id}/characters")
    public ResponseEntity<ApiResponse<CharacterProfile>> createCharacter(
            @PathVariable Long id,
            @Valid @RequestBody CharacterRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Character profile created", screenplayService.createCharacter(id, request)));
    }

    @PutMapping("/{id}/characters/{charId}")
    public ResponseEntity<ApiResponse<CharacterProfile>> updateCharacter(
            @PathVariable Long id,
            @PathVariable Long charId,
            @Valid @RequestBody CharacterRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Character profile updated", screenplayService.updateCharacter(id, charId, request)));
    }

    @DeleteMapping("/{id}/characters/{charId}")
    public ResponseEntity<ApiResponse<String>> deleteCharacter(@PathVariable Long id, @PathVariable Long charId) {
        screenplayService.deleteCharacter(id, charId);
        return ResponseEntity.ok(ApiResponse.ok("Character deleted successfully"));
    }

    // Scenes
    @GetMapping("/{id}/scenes")
    public ResponseEntity<ApiResponse<List<Scene>>> getScenes(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Scenes retrieved", screenplayService.getScenes(id)));
    }

    @PostMapping("/{id}/scenes")
    public ResponseEntity<ApiResponse<Scene>> createScene(
            @PathVariable Long id,
            @Valid @RequestBody SceneRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Scene created", screenplayService.createScene(id, request)));
    }

    @PutMapping("/{id}/scenes/{sceneId}")
    public ResponseEntity<ApiResponse<Scene>> updateScene(
            @PathVariable Long id,
            @PathVariable Long sceneId,
            @Valid @RequestBody SceneRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Scene updated", screenplayService.updateScene(id, sceneId, request)));
    }

    @DeleteMapping("/{id}/scenes/{sceneId}")
    public ResponseEntity<ApiResponse<String>> deleteScene(@PathVariable Long id, @PathVariable Long sceneId) {
        screenplayService.deleteScene(id, sceneId);
        return ResponseEntity.ok(ApiResponse.ok("Scene deleted successfully"));
    }

    // Draft Editor
    @GetMapping("/{id}/draft")
    public ResponseEntity<ApiResponse<ScreenplayDraft>> getDraft(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Screenplay draft retrieved", screenplayService.getDraft(id)));
    }

    @PutMapping("/{id}/draft")
    public ResponseEntity<ApiResponse<ScreenplayDraft>> saveDraft(
            @PathVariable Long id,
            @RequestBody DraftSaveRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Screenplay draft saved", screenplayService.saveDraft(id, request)));
    }
}
